package guessing;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class GuessingNumber {
	private static final int NUMBER_LENGTH = 4;
    private static final String DB_URL = "jdbc:sqlite:guessing_game.db";

    public static void main(String[] args) {
        setupDatabase();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your name to start the game:");
        String playerName = scanner.nextLine();
        long startTime = System.currentTimeMillis();

        String computerNumber = generateNumber();
        System.out.println(computerNumber);
        int moves = 0;
        boolean guessedCorrectly = false;

        System.out.println("Guess the 4-digit number:");
        while (!guessedCorrectly) {
            String userGuess = scanner.nextLine();
            moves++;
            if (isValidGuess(userGuess)) {
                String feedback = getFeedback(computerNumber, userGuess);
                System.out.println(feedback);
                if (feedback.equals("++++")) {
                    guessedCorrectly = true;
                }
            } else {
                System.out.println("Invalid input. Please enter a 4-digit number with unique digits.");
            }
        }
        long endTime = System.currentTimeMillis();
        long timeTaken = (endTime - startTime) / 1000; // time in seconds

        saveResult(playerName, moves, timeTaken);
        System.out.println("Congratulations " + playerName + "! You guessed the number in " + moves + " moves and " + timeTaken + " seconds.");

        String bestPlayer = getBestPlayer();
        System.out.println("Best player: " + bestPlayer);
    }

    private static String generateNumber() {
        Random random = new Random();
        StringBuilder number = new StringBuilder();
        while (number.length() < NUMBER_LENGTH) {
            int digit = random.nextInt(10);
            if (number.indexOf(String.valueOf(digit)) == -1) {
                number.append(digit);
            }
        }
        return number.toString();
    }

    private static boolean isValidGuess(String guess) {
        if (guess.length() != NUMBER_LENGTH) return false;
        Set<Character> uniqueDigits = new HashSet<>();
        for (char c : guess.toCharArray()) {
            if (!Character.isDigit(c) || !uniqueDigits.add(c)) return false;
        }
        return true;
    }

    private static String getFeedback(String computerNumber, String userGuess) {
        StringBuilder feedback = new StringBuilder();
        for (int i = 0; i < NUMBER_LENGTH; i++) {
            if (computerNumber.charAt(i) == userGuess.charAt(i)) {
                feedback.append("+");
            } else if (computerNumber.contains(String.valueOf(userGuess.charAt(i)))) {
                feedback.append("-");
            }
        }
        return feedback.toString();
    }

    private static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS results (" +
                                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                     "name TEXT NOT NULL, " +
                                     "moves INTEGER NOT NULL, " +
                                     "time_taken INTEGER NOT NULL, " +
                                     "score REAL NOT NULL);";
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveResult(String playerName, int moves, long timeTaken) {
        double score = calculateScore(moves, timeTaken);
        String insertSQL = "INSERT INTO results (name, moves, time_taken, score) VALUES (?, ?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, playerName);
            pstmt.setInt(2, moves);
            pstmt.setLong(3, timeTaken);
            pstmt.setDouble(4, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static double calculateScore(int moves, long timeTaken) {
        return 10000.0 / (moves * timeTaken); // Example formula
    }

    private static String getBestPlayer() {
        String query = "SELECT name, MIN(score) AS best_score FROM results GROUP BY name ORDER BY best_score ASC LIMIT 1;";
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getString("name") + " with score: " + rs.getDouble("best_score");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "No results yet.";
    }
}



