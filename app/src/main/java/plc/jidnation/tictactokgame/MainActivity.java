package plc.jidnation.tictactokgame;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    /**
     * Handles the game logic for a Tic-Tac-Toe board, including user interactions, game state updates, and computer moves.
     *
     * Key Components:
     * 1. Board State Variables and Maps:
     *    - Maintains the current state of the board, including player moves and available slots.
     *    - Maps button IDs to board positions for efficient lookups.
     *
     * 2. On User Click:
     *    - Locate the position of the clicked button on the board using the button's view ID.
     *    - If the button corresponds to a valid position:
     *       - Update the button UI (color and text) to reflect the user's move ("X").
     *       - Perform the following checks:
     *         - **Remove the button's position from the list of available slots.**
     *         - **Check for a win condition:**
     *             - If the user has won:
     *                 - Display a toast message indicating the win.
     *                 - Reset the game state.
     *         - **Check for a draw condition:**
     *             - If the board is full with no winner:
     *                 - Display a toast message indicating a draw.
     *                 - Reset the game state.
     *         - **If no win or draw, initiate the computer's turn.**
     *
     * 3. Computer Play:
     *    - Analyze the board to identify the user's potential next winning move.
     *    - If a winning move for the user is detected:
     *       - Block the user by selecting that position.
     *    - If no blocking is needed:
     *       - Randomly select a position from the available slots.
     *    - Update the board with the computer's move ("O").
     *    - Perform the same state checks as above (win/draw/reset).
     *
     * Game Flow:
     * - Alternates between the user's turn and the computer's turn until a win, draw, or reset occurs.
     */

    private Map<Integer, Integer> buttonIdToPosition;
    private int activePlayer = 0;
    private final Set<Integer> availableSlots = new HashSet<Integer>();
    private final Set<Integer> player0Selections = new HashSet<>();
    private final Set<Integer> player1Selections = new HashSet<>();
    private final int[][] winningPositions = {
            {1, 2, 3}, {4, 5, 6}, {7, 8, 9},  // Rows
            {1, 4, 7}, {2, 5, 8}, {3, 6, 9},  // Columns
            {1, 5, 9}, {3, 5, 7}              // Diagonals
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Map button IDs to board positions
        buttonIdToPosition = new HashMap<>();
        buttonIdToPosition.put(R.id.buttonIndex11, 1);
        buttonIdToPosition.put(R.id.buttonIndex12, 2);
        buttonIdToPosition.put(R.id.buttonIndex13, 3);
        buttonIdToPosition.put(R.id.buttonIndex21, 4);
        buttonIdToPosition.put(R.id.buttonIndex22, 5);
        buttonIdToPosition.put(R.id.buttonIndex23, 6);
        buttonIdToPosition.put(R.id.buttonIndex31, 7);
        buttonIdToPosition.put(R.id.buttonIndex32, 8);
        buttonIdToPosition.put(R.id.buttonIndex33, 9);
        availableSlots.addAll(buttonIdToPosition.values());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    public void buttonClicked(View view) {
        Button selectedButton = (Button) view;

        // Alternate active player
        activePlayer = 0;

        // Update button UI
        updateButtonUI(selectedButton);

        // Store selection
        storeSelection(view.getId());

        //Check game state
        boolean canContinue = checkGameState();

        if(canContinue) initiateComputerPlay();

    }

    private void initiateComputerPlay() {
        activePlayer = 1;
        int computerPosition = findBlockingMove();

        if (computerPosition == -1) {
            // No blocking move found, choose a random available slot
            int computerSelection = (int) (Math.random() * availableSlots.size());
            computerPosition = availableSlots.toArray(new Integer[0])[computerSelection];
        }

        Button computerButton = findViewById(getKeyFromValue(computerPosition));
        updateButtonUI(computerButton);
        storeSelection(computerButton.getId());
         checkGameState();
    }

    private Integer getKeyFromValue( int value) {
        for (Map.Entry<Integer, Integer> entry : buttonIdToPosition.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey(); // Return the key
            }
        }
        return null; // Return null if no matching key is found
    }

    private int findBlockingMove() {
        // Iterate through winning positions
        for (int[] winCombo : winningPositions) {
            int userMarks = 0;
            int emptySlot = -1;

            // Check each position in the combo
            for (int position : winCombo) {
//                [1,2,3]
                if (player0Selections.contains(position)) {
                    userMarks++;
                } else if (!player1Selections.contains(position) && availableSlots.contains(position)) {
                    emptySlot = position;
                }
            }

            // If the user is about to win, return the empty slot to block
            if (userMarks == 2 && emptySlot != -1) {
                return emptySlot;
            }
        }

        // No blocking move found
        return -1;
    }

    private boolean checkGameState() {
        boolean isWon = checkWin();
        if(isWon){
            sendOutGreetings();
            resetGame();
            return  false;
        }else{
            boolean isDraw = checkDraw();
            return  !isDraw;
        }
    }

    private void storeSelection(int id) {
        int position = buttonIdToPosition.get(id);
        if (activePlayer == 0) {
            player0Selections.add(position);
        } else {
            player1Selections.add(position);
        }
        availableSlots.remove(id);
        System.out.println("::::From the selected room ::: " + id );
    }

    private void updateButtonUI(Button selectedButton) {
        if (activePlayer == 0) {
            selectedButton.setText("X");
            selectedButton.setBackgroundColor(Color.RED);
        } else {
            selectedButton.setText("O");
            selectedButton.setBackgroundColor(Color.GREEN);
        }
        selectedButton.setEnabled(false);
    }

    private boolean checkDraw() {
        boolean isDraw = player0Selections.size() + player1Selections.size() == 9;
        if(isDraw){
            Toast.makeText(this, "It's a draw!", Toast.LENGTH_LONG).show();
            resetGame();
            return  true;
        }
        return false;
    }

    private void sendOutGreetings() {
        // Send a greeting message to the user who won
        String greeting = "Player " + (activePlayer == 0 ? "X" : "O") + " has won!";
        Toast.makeText(this, greeting, Toast.LENGTH_LONG).show();
    }

    private void resetGame() {
        player0Selections.clear();
        player1Selections.clear();
        availableSlots.clear();
        availableSlots.addAll(buttonIdToPosition.values());

        activePlayer = 0;

        //Resetting the buttons
        for(int buttonId : buttonIdToPosition.keySet()){
            Button button = findViewById(buttonId);
            button.setText("");
            button.setEnabled(true);
            button.setBackgroundColor(Color.LTGRAY);
        }
    }


    private boolean checkWin() {
        Set<Integer> activeSelections = activePlayer == 0 ? player0Selections : player1Selections;
        for(int[] winningPosition : winningPositions){
            if(activeSelections.contains(winningPosition[0]) &&
                    activeSelections.contains(winningPosition[1]) &&
                    activeSelections.contains(winningPosition[2])){
                return  true;
            }
        }
        return false;
    }
}