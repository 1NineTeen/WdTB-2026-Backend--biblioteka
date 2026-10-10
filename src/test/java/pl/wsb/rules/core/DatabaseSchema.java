package pl.wsb.rules.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Migawka schematu bazy odczytana przez JDBC ({@link DatabaseMetaData}): to samo, co widzicie w konsoli H2.
 */
public record DatabaseSchema(
        Set<String> tables,
        Map<String, List<String>> primaryKeys,
        List<ForeignKey> foreignKeys,
        Map<String, Set<String>> uniqueColumns,
        List<Column> columns) {

    public static final String DEFAULT_TABLE = "user_account";
    private static final Set<String> TECHNICAL_TABLES = Set.of("flyway_schema_history");

    public record ForeignKey(String table, String column, String referencedTable) {
    }

    public record Column(String table, String name, String type) {
    }

    public static DatabaseSchema read(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            String schema = connection.getSchema();
            Map<String, String> rawNames = new HashMap<>();
            try (ResultSet rs = meta.getTables(null, schema, "%", null)) {
                while (rs.next()) {
                    String type = rs.getString("TABLE_TYPE").toUpperCase(Locale.ROOT);
                    String name = lower(rs.getString("TABLE_NAME"));
                    if (type.contains("TABLE") && !type.contains("SYSTEM") && !TECHNICAL_TABLES.contains(name)) {
                        rawNames.put(name, rs.getString("TABLE_NAME"));
                    }
                }
            }
            Map<String, List<String>> primaryKeys = new HashMap<>();
            List<ForeignKey> foreignKeys = new ArrayList<>();
            Map<String, Set<String>> uniqueColumns = new HashMap<>();
            List<Column> columns = new ArrayList<>();
            for (var entry : rawNames.entrySet()) {
                String table = entry.getKey();
                String raw = entry.getValue();
                try (ResultSet rs = meta.getPrimaryKeys(null, schema, raw)) {
                    while (rs.next()) {
                        primaryKeys.computeIfAbsent(table, t -> new ArrayList<>()).add(lower(rs.getString("COLUMN_NAME")));
                    }
                }
                try (ResultSet rs = meta.getImportedKeys(null, schema, raw)) {
                    while (rs.next()) {
                        foreignKeys.add(new ForeignKey(table, lower(rs.getString("FKCOLUMN_NAME")),
                                lower(rs.getString("PKTABLE_NAME"))));
                    }
                }
                Map<String, List<String>> uniqueIndexes = new HashMap<>();
                try (ResultSet rs = meta.getIndexInfo(null, schema, raw, true, false)) {
                    while (rs.next()) {
                        if (rs.getString("COLUMN_NAME") != null) {
                            uniqueIndexes.computeIfAbsent(rs.getString("INDEX_NAME"), i -> new ArrayList<>())
                                    .add(lower(rs.getString("COLUMN_NAME")));
                        }
                    }
                }
                uniqueIndexes.values().stream()
                        .filter(cols -> cols.size() == 1)
                        .forEach(cols -> uniqueColumns.computeIfAbsent(table, t -> new HashSet<>()).add(cols.get(0)));
                List<String> pk = primaryKeys.getOrDefault(table, List.of());
                if (pk.size() == 1) {
                    uniqueColumns.computeIfAbsent(table, t -> new HashSet<>()).add(pk.get(0));
                }
                try (ResultSet rs = meta.getColumns(null, schema, raw, "%")) {
                    while (rs.next()) {
                        columns.add(new Column(table, lower(rs.getString("COLUMN_NAME")),
                                rs.getString("TYPE_NAME").toUpperCase(Locale.ROOT)));
                    }
                }
            }
            return new DatabaseSchema(rawNames.keySet(), primaryKeys, foreignKeys, uniqueColumns, columns);
        } catch (SQLException e) {
            throw new IllegalStateException("Nie udało się odczytać schematu bazy", e);
        }
    }

    /** Tabele zespołu: bez gotowej tabeli z szablonu. */
    public Set<String> ownTables() {
        Set<String> own = new HashSet<>(tables);
        own.remove(DEFAULT_TABLE);
        return own;
    }

    public List<ForeignKey> foreignKeysOf(String table) {
        return foreignKeys.stream().filter(fk -> fk.table().equals(table)).toList();
    }

    public boolean isUnique(String table, String column) {
        return uniqueColumns.getOrDefault(table, Set.of()).contains(column);
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }
}
