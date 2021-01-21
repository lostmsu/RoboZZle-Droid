/**
 * 
 */
package com.team242.robozzle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.team242.robozzle.RobozzleWebClient.LevelVoteInfo;
import com.team242.robozzle.model.Puzzle;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author lost
 *
 */
public class RobozzleOffline extends OrmLiteSqliteOpenHelper {
	private static final int VERSION = 8;
	public static final String NAME = "ROBOZZLE";
//	private static final String PUZZLES_TABLE = "PUZZLES";
//	private static final String CREATE_PUZZLES = "CREATE " + PUZZLES_TABLE + " (" +
		
	
	public RobozzleOffline(Context context) {
		super(context, NAME, null, VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connection) {
		try {
			TableUtils.createTable(connection, Puzzle.class);
			TableUtils.createTable(connection, EditHistoryItem.class);
		} catch (SQLException e) {
			Log.e(RobozzleOffline.class.getName(), "Create database failed", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connection, int verOld, int verNew) {
		try {
			for(; verOld < verNew; verOld++){
				switch(verOld){
				case 1:
					upgrade1_2(db);
					break;
				case 2:
					upgrade2_3(db);
					break;
				case 3:
					upgrade3_4(db);
					break;
				case 4:
					upgrade4_5(connection);
					break;
				case 5:
					upgrade5_6(db);
					break;
				case 6:
					upgrade6_7(db);
					break;
				case 7:
					upgrade7_8(db);
					break;
				}
			}
		} catch (SQLException e) {
			Log.e(RobozzleOffline.class.getName(), "Upgrade database failed", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void upgrade7_8(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE PUZZLES ADD COLUMN hasNewSolution SMALLINT DEFAULT 0");
	}

	private void upgrade6_7(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE PUZZLES ADD COLUMN scary INTEGER NOT NULL DEFAULT 0");
	}

	private void upgrade5_6(SQLiteDatabase db){
		db.execSQL("ALTER TABLE PUZZLES ADD COLUMN userLike INTEGER NOT NULL DEFAULT 0");
		db.execSQL("ALTER TABLE PUZZLES ADD COLUMN userDifficulty INTEGER NOT NULL DEFAULT 0");
	}

	private void upgrade4_5(ConnectionSource connection){
		try{
			TableUtils.createTable(connection, EditHistoryItem.class);
		} catch (SQLException e) {
			Log.e(RobozzleOffline.class.getName(), "Create database failed", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void upgrade3_4(SQLiteDatabase db){
		db.execSQL("ALTER TABLE PUZZLES ADD COLUMN program varchar(250)");
	}
	
	private void upgrade2_3(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE PUZZLES ADD COLUMN subLengths varchar(5)");
	}


	private void upgrade1_2(SQLiteDatabase db) throws SQLException {
		db.execSQL("ALTER TABLE PUZZLES ADD COLUMN solution varchar(250)");
	}

	private Dao<Puzzle, Integer> puzzlesDAO;
	public Dao<Puzzle, Integer> getPuzzlesDAO(){
		if (puzzlesDAO == null){
			try {
				puzzlesDAO = DaoManager.createDao(getConnectionSource(), Puzzle.class);
			} catch (SQLException e) {
				Log.e(RobozzleOffline.class.getName(), "Create puzzles DAO failed", e);
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return puzzlesDAO;
	}
	
	public List<Puzzle> getAllPuzzles() throws SQLException {
		getPuzzlesDAO();
		
		QueryBuilder<Puzzle, Integer> query = puzzlesDAO.queryBuilder();
		return puzzlesDAO.query(query.prepare());
	}
	
	public List<Puzzle> synchronize(List<Integer> solved, List<LevelVoteInfo> votes, String userName, String password) throws SQLException, NoSuchAlgorithmException, IOException, XmlPullParserException{
		getPuzzlesDAO();

		List<Puzzle> puzzles = getAllPuzzles();
		HashSet<Integer> solvedSet = new HashSet<Integer>(solved);
		
		RobozzleWebClient web = new RobozzleWebClient();
		
		List<Puzzle> newSolutions = new ArrayList<Puzzle>();
		
		for(Puzzle puzzle: puzzles){
			if (puzzle.id < 0) continue;
			
			if (solvedSet.contains(puzzle.id) && !puzzle.hasNewSolution){
				if (puzzle.getSolution() == null){
					puzzle.setSolution("");
					newSolutions.add(puzzle);
				}
			} else if (puzzle.getSolution() != null){
				String solution = puzzle.getSolutionProgram().getProgram().replace("__", "");
				String result = web.SubmitSolution(puzzle.id, userName, password, solution);
				if (result != null){
					Exception error = new InvalidAlgorithmParameterException("Solution " + solution + 
							" does not fits puzzle " + puzzle.title + " according to RoboZZle.com");
					// TODO: report exception
				} else {
					puzzle.hasNewSolution = false;

					puzzlesDAO.update(puzzle);
				}
			}
		}

		if (newSolutions.size() == 0) return null;
		return newSolutions;
	}
	
	private Dao<EditHistoryItem, Integer> history;

	public Dao<EditHistoryItem, Integer> getHistoryDAO() {
		if (history == null) {
			try {
				history = DaoManager.createDao(getConnectionSource(),
						EditHistoryItem.class);
			} catch (SQLException e) {
				Log.e(RobozzleOffline.class.getName(),
						"Create history DAO failed", e);
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		return history;
	}
	
	@Override
	public void close() {
		super.close();
		puzzlesDAO = null;
	}
}
