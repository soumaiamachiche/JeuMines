package mine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class Board extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int COVER_CELL = 10;
    private static final int MARK_CELL = 10;
    private static final int EMPTY_CELL = 0;
    private static final int MINE_CELL = 9;
    private static final int COVERED_MINE_CELL = MINE_CELL + COVER_CELL;
    private static final int DRAW_MINE = 9;
    private static final int DRAW_COVER = 10;
    private static final int DRAW_MARK = 11;
    private static final int DRAW_WRONG_MARK = 12;
    private static final int CELL_SIZE = 15;
    private static final int NUM_IMAGES = 13;

    private static final int totalMines = 40;
    private static final int rows = 16;
    private static final int cols = 16;
    private static final Random random = new Random();

    private int totalCells;
    private int[] field;
    private boolean inGame;
    private int minesLeft;
    private transient Image[] images;
    private final JLabel statusbar;

    public Board(JLabel statusbar) {
        this.statusbar = statusbar;
        this.images = new Image[NUM_IMAGES];
        initializeImages();
        setDoubleBuffered(true);
        addMouseListener(new MinesAdapter());
        startNewGame();
    }

    private void initializeImages() {
        for (int i = 0; i < NUM_IMAGES; i++) {
            images[i] = new ImageIcon(getClass().getClassLoader().getResource(i + ".gif")).getImage();
        }
    }

    public void startNewGame() {
        inGame = true;
        minesLeft = totalMines;
        totalCells = rows * cols;
        field = new int[totalCells];

        for (int i = 0; i < totalCells; i++) {
            field[i] = COVER_CELL;
        }
        statusbar.setText(Integer.toString(minesLeft));
        placeMines();
        updateCellNumbers();
    }

    private void placeMines() {
        int minesPlaced = 0;
        while (minesPlaced < totalMines) {
            int position = random.nextInt(totalCells);
            if (field[position] != COVERED_MINE_CELL) {
                field[position] = COVERED_MINE_CELL;
                minesPlaced++;
            }
        }
    }

    private void updateCellNumbers() {
        for (int position = 0; position < totalCells; position++) {
            if (field[position] == COVERED_MINE_CELL) {
                updateAdjacentCells(position);
            }
        }
    }

    private void updateAdjacentCells(int position) {
        int[] neighbors = {-1, 1, -cols, cols, -cols - 1, -cols + 1, cols - 1, cols + 1};
        for (int offset : neighbors) {
            int neighborPos = position + offset;
            if (isWithinBounds(neighborPos) && field[neighborPos] != COVERED_MINE_CELL) {
                field[neighborPos]++;
            }
        }
    }

    private boolean isWithinBounds(int position) {
        return position >= 0 && position < totalCells;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int uncoveredCells = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int cell = field[(i * cols) + j];
                if (!inGame) {
                    cell = getPostGameCellType(cell);
                } else {
                    if (cell > COVERED_MINE_CELL) cell = DRAW_MARK;
                    else if (cell > MINE_CELL) cell = DRAW_COVER;
                    else uncoveredCells++;
                }
                g.drawImage(images[cell], (j * CELL_SIZE), (i * CELL_SIZE), this);
            }
        }
        updateStatus(uncoveredCells);
    }

    private int getPostGameCellType(int cell) {
        if (cell == COVERED_MINE_CELL) return DRAW_MINE;
        else if (cell > COVERED_MINE_CELL) return DRAW_WRONG_MARK;
        else if (cell > MINE_CELL) return DRAW_COVER;
        return cell;
    }

    private void updateStatus(int uncoveredCells) {
        if (uncoveredCells == 0 && inGame) {
            inGame = false;
            statusbar.setText("Game won");
        } else if (!inGame) {
            statusbar.setText("Game lost");
        }
    }

    private void findEmptyCells(int position) {
        int[] neighbors = {-1, 1, -cols, cols, -cols - 1, -cols + 1, cols - 1, cols + 1};
        for (int offset : neighbors) {
            int neighborPos = position + offset;
            if (isWithinBounds(neighborPos) && field[neighborPos] > MINE_CELL) {
                field[neighborPos] -= COVER_CELL;
                if (field[neighborPos] == EMPTY_CELL) {
                    findEmptyCells(neighborPos);
                }
            }
        }
    }

    private class MinesAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (!inGame) {
                startNewGame();
                repaint();
                return;
            }
            handleMouseClick(e);
        }

        private void handleMouseClick(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cCol = x / CELL_SIZE;
            int cRow = y / CELL_SIZE;
            int position = (cRow * cols) + cCol;

            if (position < totalCells) {
                if (e.getButton() == MouseEvent.BUTTON3) handleRightClick(position);
                else handleLeftClick(position);
                repaint();
            }
        }

        private void handleRightClick(int position) {
            if (field[position] > MINE_CELL) {
                if (field[position] <= COVERED_MINE_CELL) {
                    if (minesLeft > 0) {
                        field[position] += MARK_CELL;
                        minesLeft--;
                    } else statusbar.setText("No marks left");
                } else {
                    field[position] -= MARK_CELL;
                    minesLeft++;
                }
                statusbar.setText(Integer.toString(minesLeft));
            }
        }

        private void handleLeftClick(int position) {
            if (field[position] <= COVERED_MINE_CELL && field[position] > MINE_CELL) {
                field[position] -= COVER_CELL;
                if (field[position] == EMPTY_CELL) findEmptyCells(position);
                if (field[position] == MINE_CELL) inGame = false;
            }
        }
    }
}
