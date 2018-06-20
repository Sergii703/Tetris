import java.awt.*; //библиотека для внешних форма
import java.awt.event.*; //библиотека событий
import javax.swing.*; //библиотека для гафических элементов
import java.util.*; //библиотека для рандомных чисел


public class Tetris{

	//константы
	final String TITLE_OF_PROGRAM = "Tetris";
	final int BLOCK_SIZE = 25;
	final int ARC_RADIUS = 6;
	final int FIELD__WIDTH = 20;
	final int FIELD__HEIGHT = 30;
	final int START_LOCATION = 180;
	final int FIELD_DX = 7;
	final int FIELD_DY = 26;
	final int LEFT = 37;
	final int UP = 38;
	final int RIGHT = 39;
	final int DOWN = 40;
	final int SHOW_DELAY = 350; //delay for annimation
	final int [][][] SHAPES = {
		{{0, 0, 0, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}, {4, 0xcc99ff}},
		{{0, 0, 0, 0}, {0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {4, 0xffff00}},
		{{1, 0, 0, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0x29a329}},
		{{0, 0, 1, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xe62e00}},
		{{0, 1, 1, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0x3366ff}},
		{{1, 1, 1, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xff33ff}},
		{{1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xff661a}},
	};
	final int[] SCORES = {100, 300, 700, 1500}; //очки в зависимости от кол-ва заполненых строк
	int gameScore = 0;
	int [][] mine = new int[FIELD__HEIGHT+1][FIELD__WIDTH];
	JFrame frame;
	Canvas canvasPanel = new Canvas();
	Random random = new Random();
	Figure figure = new Figure();
	boolean gameOver = false;
	final int[][] GAME_OVER_MSG = {
		{0,1,1,0,0,0,1,1,0,0,0,1,0,1,0,0,0,1,1,0},
		{1,0,0,0,0,1,0,0,1,0,1,0,1,0,1,0,1,0,0,1},
		{1,0,1,1,0,1,1,1,1,0,1,0,1,0,1,0,1,1,1,1},
		{1,0,0,1,0,1,0,0,1,0,1,0,1,0,1,0,1,0,0,0},
		{0,1,1,0,0,1,0,0,1,0,1,0,1,0,1,0,0,1,1,0},
		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,1,1,0,0,1,0,0,1,0,0,1,1,0,0,1,1,1,0,0},
		{1,0,0,1,0,1,0,0,1,0,1,0,0,1,0,1,0,0,1,0},
		{1,0,0,1,0,1,0,1,0,0,1,1,1,1,0,1,1,1,0,0},
		{1,0,0,0,0,1,1,0,0,0,1,0,0,0,0,1,0,0,1,0},
		{0,1,1,0,0,1,0,0,0,0,0,1,1,0,0,1,0,0,1,0}};


	public static void main(String[] args){
		new Tetris().go();
	}

	public void go(){
		frame = new JFrame(TITLE_OF_PROGRAM);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(FIELD__WIDTH * BLOCK_SIZE + FIELD_DX, FIELD__HEIGHT*BLOCK_SIZE+FIELD_DY);
		frame.setLocation(START_LOCATION, START_LOCATION);
		frame.setResizable(false);

		canvasPanel.setBackground(Color.BLACK);
		

		frame.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				if(!gameOver){
					if(e.getKeyCode()==DOWN) figure.drop();
					if(e.getKeyCode()==UP) figure.rotate();
					if(e.getKeyCode()==LEFT || e.getKeyCode()==RIGHT) figure.move(e.getKeyCode()); 
				}
				canvasPanel.repaint();
			}
		});
		frame.getContentPane().add(BorderLayout.CENTER, canvasPanel);
		frame.setVisible(true);

		Arrays.fill(mine[FIELD__HEIGHT], 1);

		//главный цикл игры
		while(!gameOver){
			try{
				Thread.sleep(SHOW_DELAY);
			} catch(Exception e){e.printStackTrace();}
			canvasPanel.repaint();
			if(figure.isTouchGround()){
				figure.leaveOnTheGround();
				checkFilling();
				figure = new Figure();
				gameOver = figure.isCrossGround();
			}else{
				figure.stepDown();
			}
		}
	}

	void checkFilling(){
		int row = FIELD__HEIGHT - 1;
		int countFillRows = 0;
		while(row>0){
			int filled = 1;
			for (int col = 0; col<FIELD__WIDTH; col++)
				filled *= Integer.signum(mine[row][col]);
			if (filled>0){
				countFillRows++;
				for (int i = row; i>0; i--) System.arraycopy(mine[i-1], 0, mine[i], 0, FIELD__WIDTH);
			}else{
				row--;
			}
		}
		if (countFillRows>0) {
			gameScore += SCORES[countFillRows - 1];
			frame.setTitle(TITLE_OF_PROGRAM + " : " + gameScore);
		}
	}

	class Figure{
		private ArrayList<Block> figure = new ArrayList<Block>();
		private int[][] shape = new int[4][4];
		private int type, size, color;
		private int x=3, y=0;

		Figure(){
			type = random.nextInt(SHAPES.length);
			size = SHAPES[type][4][0];
			color = SHAPES[type][4][1];

			if (size == 4) y = -1;

			for (int i = 0; i < size; i++)
				System.arraycopy(SHAPES[type][i], 0, shape[i], 0, SHAPES[type][i].length);
			createFormShape();
		}

		void createFormShape(){
			for (int x= 0; x<size; x++)
				for (int y=0; y<size; y++)
					if(shape[y][x] == 1) figure.add(new Block(x+this.x, y+this.y));
		}

		boolean isTouchGround(){
			for (Block block : figure) 
				if (mine[block.getY()+1][block.getX()]>0) 
					return true;
			return false;
		}

		boolean isCrossGround(){
			for (Block block : figure) 
				if (mine[block.getY()][block.getX()]>0) 
					return true;
			return false;
		}

		void leaveOnTheGround(){
			for (Block block : figure) mine[block.getY()][block.getX()] = color;
		}

		void stepDown(){
			for (Block block : figure) block.setY(block.getY()+1);
				y++;
		}

		void drop(){
			while(!isTouchGround()) stepDown();
		}

		boolean isTouchWall(int direction){
			for (Block block : figure) {
				if (direction == LEFT && (block.getX() == 0 || mine [block.getY()][block.getX() - 1] > 0)) return true;
				if (direction == RIGHT && (block.getX() == FIELD__WIDTH -1 || mine [block.getY()][block.getX() + 1] > 0)) return true;
			}
			return false;
		}

		void move(int direction){
			if (!isTouchWall(direction)){
				int dx = direction - 38;
				for (Block block : figure) block.setX(block.getX()+dx);
				x+=dx;
			}
		}

		boolean isWrongPosition(){
			for (int x = 0;x<size;x++) 
				for (int y = 0;y<size;y++) 
					if (shape[x][y] == 1){
						if (y+this.y<0) return true; 
						if (x+this.x<0 || x + this.x > FIELD__WIDTH - 1) return true;
						if (mine[y+this.y][x + this.x] > 0) return true;	
					}
			return false;
		}

		void rotate(){
			for (int i = 0; i<size/2; i++)
				for (int j = i; j< size-1-i; j++){
					int tmp = shape[size-1-j][i];
					shape[size-1-j][i] = shape[size-1-i][size-1-j];
					shape[size-1-i][size-1-j] = shape[j][size-1-i];
					shape[j][size-1-i] = shape[i][j];
					shape[i][j] = tmp;
				}
			if (!isWrongPosition()) {
				figure.clear();
				createFormShape();
			}
		}

		void paint(Graphics g){
			for (Block block : figure) block.paint(g, color);
		}
	}

	class Block{
		private int x, y;

		public Block(int x, int y){
			setX(x);
			setY(y);
		}
		void setX(int x){this.x = x;}
		void setY(int y){this.y = y;}

		int getX(){return x;}
		int getY(){return y;}

		void paint(Graphics g, int color){
			g.setColor(new Color(color));
			g.drawRoundRect(x*BLOCK_SIZE+1, y*BLOCK_SIZE+1, BLOCK_SIZE-2, BLOCK_SIZE-2, ARC_RADIUS, ARC_RADIUS);
		}
	}

	public class Canvas extends JPanel{
		@Override
		public void paint(Graphics g){
			super.paint(g);
			for (int x = 0; x<FIELD__WIDTH; x++)
				for (int y = 0;y<FIELD__HEIGHT; y++)
					if(mine[y][x]>0){
						g.setColor(new Color(mine[y][x]));
						g.fill3DRect(x*BLOCK_SIZE+1, y*BLOCK_SIZE+1, BLOCK_SIZE-1, BLOCK_SIZE-1, true);
					}
			if (gameOver){
				g.setColor(Color.white);
				for (int y = 0; y < GAME_OVER_MSG.length; y++) 
					for (int x = 0; x < GAME_OVER_MSG[y].length; x++)
						if (GAME_OVER_MSG[y][x] == 1) g.fill3DRect(x*11+18, y*11+160, 10, 10, true);
				
			}else
			figure.paint(g);
		}
	}
}