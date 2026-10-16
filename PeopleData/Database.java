import java.sql.*;

public class Database {
    // koneksi ke database MySQL
    private Connection connection;
    // statement dipakai untuk menjalankan query SQL
    private Statement statement;

    // constructor
    public Database() {
        try {
            // membuka koneksi ke database db_people
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/db_people",
                    "root",
                    "");
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // digunakan untuk SELECT
    public ResultSet selectQuery(String sql) {
        try {
            statement.executeQuery(sql);
            return statement.getResultSet();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // digunakan untuk UPDATE, INSERT, DELETE
    public int insertUpdateDeleteQuery(String sql) {
        try {
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // getter
    public Statement getStatement() {
        return statement;
    }
}