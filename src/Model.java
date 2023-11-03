import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    boolean isSaveNeeded = true;
    int score = 0;
    int maxTile = 2;
    Stack<Tile[][]> previousStates = new Stack<>();
    Stack<Integer> previousScores = new Stack<>();


    Model() {
        resetGameTiles();
    }

    public Tile[][]getGameTiles(){
        return gameTiles;
    }
    public int getScore(){
        return score;
    }
    void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (!emptyTiles.isEmpty()) {
            int index = (int) (Math.random() * emptyTiles.size()) % emptyTiles.size();
            Tile emptyTile = emptyTiles.get(index);
            emptyTile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private List<Tile> getEmptyTiles() {
        final List<Tile> list = new ArrayList<>();
        for (Tile[] tarr : gameTiles) {
            for (Tile t : tarr) {
                if (t.isEmpty()) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean res = false;
        LinkedList<Tile> tilesList = new LinkedList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (tiles[i].isEmpty()) {
                continue;
            }
            if (i < FIELD_WIDTH - 1 && tiles[i].value == tiles[i + 1].value) {
                int updatedValue = tiles[i].value * 2;
                if (updatedValue > maxTile) {
                    maxTile = updatedValue;
                }
                score += updatedValue;
                tilesList.addLast(new Tile(updatedValue));
                tiles[i + 1].value = 0;
                res = true;
            } else {
                tilesList.addLast(new Tile(tiles[i].value));
            }
            tiles[i].value = 0;
        }
        for (int i = 0; i < tilesList.size(); i++) {
            tiles[i] = tilesList.get(i);
        }
        return res;
    }


    private boolean compressTiles(Tile[] tiles) {
        int insertPos = 0;
        boolean res = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (!tiles[i].isEmpty()) {
                if (i != insertPos) {
                    tiles[insertPos] = tiles[i];
                    tiles[i] = new Tile();
                    res = true;
                }
                insertPos++;
            }
        }
        return res;
    }

    private Tile[][] rotateClockwise(Tile[][] tiles){
        final int N = tiles.length;
        Tile[][] res = new Tile[N][N];
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                res[j][N-1-i] = tiles[i][j];
            }
        }
        return res;
    }

    public void left(){
        if(isSaveNeeded){
            saveState(gameTiles);
        }

        boolean moveFlag = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) {
                moveFlag = true;
            }
        }
            if(moveFlag) {
                addTile();
            }
            isSaveNeeded = true;
    }

    public void up(){
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
    }

    public void down(){
        saveState(gameTiles);
        gameTiles  =rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
    }

    public void right(){
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
    }
    public int getEmptyTilesCount(){
        return getEmptyTiles().size();
    }
    public boolean isFull(){
        return getEmptyTilesCount()==0;
    }
    public boolean canMove(){
        if(!isFull()){
            return true;
        }
        for(int x =0;x<FIELD_WIDTH;x++){
            for(int y=0;y<FIELD_WIDTH;y++){
                Tile t = gameTiles[x][y];
                if((x<FIELD_WIDTH-1 && t.value==gameTiles[x+1][y].value)
                    || (y<FIELD_WIDTH-1 && t.value == gameTiles[x][y+1].value)){
                    return true;
                }
            }
        }
        return false;
    }
    public void saveState(Tile[][] tiles){
        Tile[][] tempTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i=0;i<FIELD_WIDTH;i++){
            for(int j=0;j<FIELD_WIDTH;j++){
                tempTiles[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(tempTiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }
    public void rollback(){
        if(!previousStates.isEmpty() && !previousScores.isEmpty()){
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }
    public void randomMove(){
        int n;
        n=((int)(Math.random()*100))%4;
        switch(n){
            case 0:
                left();
                break;
            case 1:
                up();
                break;
            case 2:
                down();
                break;
            case 3:
                right();
                break;
        }
    }
    private MoveEfficiency getMoveEfficiency(Move move){
        MoveEfficiency moveEfficiency = new MoveEfficiency(-1,0,move);
        move.move();
        if(hasBoardChanged()){
            moveEfficiency = new MoveEfficiency(getEmptyTilesCount(),score,move);
        }
        rollback();
        return moveEfficiency;
    }

    private boolean hasBoardChanged(){
        for(int i=0;i<FIELD_WIDTH;i++){
            for(int j=0;j<FIELD_WIDTH;j++){
                if(gameTiles[i][j].value != previousStates.peek()[i][j].value){
                    return true;
                }
            }
        }
        return false;
    }
    public void autoMove(){
         PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4,Collections.reverseOrder());
        queue.offer(getMoveEfficiency(this::left));
        queue.offer(getMoveEfficiency(this::right));
        queue.offer(getMoveEfficiency(this::down));
        queue.offer(getMoveEfficiency(this::up));
        queue.peek().getMove().move();
    }
}
