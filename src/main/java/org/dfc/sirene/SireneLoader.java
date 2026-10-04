package org.dfc.sirene;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.postgresql.PGConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Pour chaque dataset : crée la table si absente (schéma issu du descripteur csv),
 * puis la vide et la recharge par COPY. Le csv de données n'est jamais lu par Java :
 * il est streamé tel quel vers PostgreSQL.
 */
@Component
class SireneLoader implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SireneLoader.class);

    private record Column(String name, int length, String type) {
        String sqlType() {
            return switch (type) {
                case "Date" -> length == 4 ? "smallint" : length > 10 ? "timestamp" : "date"; // longueur 4 = année
                case "Numérique" -> "integer";
                default -> length > 0 ? "varchar(" + length + ")" : "text"; // longueur "null" => text
            };
        }
    }

    private final DataSource dataSource;
    private final SireneProperties props;

    SireneLoader(DataSource dataSource, SireneProperties props) {
        this.dataSource = dataSource;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (var e : props.datasets()) {
            var columns = readDescriptor(Path.of(props.descriptorsDir(), e.descriptor()));
            load(e.table(), Path.of(props.datasetsDir(), e.dataset()), columns);
        }
    }

    /** Colonnes triées par Ordre. Format : Nom,Libellé,Longueur,Type,Ordre (sans virgule dans le libellé). */
    private static List<Column> readDescriptor(Path file) throws IOException {
        return Files.readAllLines(file, StandardCharsets.UTF_8).stream().skip(1)
                .filter(l -> !l.isBlank())
                .map(l -> l.split(",", -1))
                .sorted((a, b) -> Integer.compare(Integer.parseInt(a[4].trim()), Integer.parseInt(b[4].trim())))
                .map(f -> new Column(f[0].trim().toLowerCase(), parseLength(f[2]), f[3].trim().split(":")[0]))
                .toList();
    }

    private static int parseLength(String s) {
        return s.trim().matches("\\d+") ? Integer.parseInt(s.trim()) : 0;
    }

    private void load(String table, Path csv, List<Column> columns) throws SQLException, IOException {
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS " + table + " (" + columns.stream()
                        .map(col -> col.name() + " " + col.sqlType()).collect(Collectors.joining(", ")) + ")");
                st.execute("TRUNCATE " + table);
            }
            String sql = "COPY " + table + " (" + columns.stream().map(Column::name).collect(Collectors.joining(", "))
                    + ") FROM STDIN WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')";
            try (Reader in = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
                long rows = c.unwrap(PGConnection.class).getCopyAPI().copyIn(sql, in);
                c.commit();
                log.info("{} <- {} : {} lignes", table, csv.getFileName(), rows);
            } catch (SQLException | IOException ex) {
                c.rollback();
                throw ex;
            }
        }
    }
}
