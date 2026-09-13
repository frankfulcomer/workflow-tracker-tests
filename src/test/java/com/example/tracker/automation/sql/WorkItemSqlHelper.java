package com.example.tracker.automation.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin, transparent wrapper around raw parameterized SQL against the app's
 * work_items / status_history / owners tables. Plain JDBC only - no ORM, no
 * application source/model classes. Table and column names here come from
 * reading the app's entity annotations (Hibernate's ddl-auto=update derives
 * the schema directly from them), not from importing any of that code.
 */
public class WorkItemSqlHelper {

    public record WorkItemRow(String title, String description, String status, Long ownerId, String ownerName) {}

    public record HistoryRow(String previousStatus, String newStatus) {}

    private final Connection connection;

    public WorkItemSqlHelper(Connection connection) {
        this.connection = connection;
    }

    public WorkItemRow findWorkItem(long id) throws SQLException {
        String query = "SELECT wi.title, wi.description, wi.status, wi.owner_id, o.name AS owner_name "
                + "FROM work_items wi LEFT JOIN owners o ON o.id = wi.owner_id "
                + "WHERE wi.id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("No work item with id " + id);
                }
                return new WorkItemRow(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getObject("owner_id", Long.class),
                        rs.getString("owner_name"));
            }
        }
    }

    public int countHistoryRows(long workItemId) throws SQLException {
        String query = "SELECT COUNT(*) FROM status_history WHERE work_item_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, workItemId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** All history rows for the item, oldest first. */
    public List<HistoryRow> historyRows(long workItemId) throws SQLException {
        String query = "SELECT previous_status, new_status FROM status_history "
                + "WHERE work_item_id = ? ORDER BY changed_at ASC";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, workItemId);
            try (ResultSet rs = statement.executeQuery()) {
                List<HistoryRow> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new HistoryRow(rs.getString("previous_status"), rs.getString("new_status")));
                }
                return rows;
            }
        }
    }

    public HistoryRow latestHistoryRow(long workItemId) throws SQLException {
        String query = "SELECT previous_status, new_status FROM status_history "
                + "WHERE work_item_id = ? ORDER BY changed_at DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, workItemId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("No history rows for work item " + workItemId);
                }
                return new HistoryRow(rs.getString("previous_status"), rs.getString("new_status"));
            }
        }
    }
}
