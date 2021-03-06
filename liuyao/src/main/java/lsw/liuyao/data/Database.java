package lsw.liuyao.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import lsw.library.CrossAppKey;
import lsw.library.DatabaseManager;
import lsw.library.DateExt;
import lsw.liuyao.R;
import lsw.liuyao.common.MyApplication;
import lsw.liuyao.data.future.DailyData;
import lsw.liuyao.model.HexagramLineNote;
import lsw.liuyao.model.HexagramRow;
import lsw.liuyao.model.ImageAttachment;
import lsw.utility.FileHelper;

/**
 * Created by swli on 8/18/2015.
 */
public class Database extends DatabaseManager {

    private Context context;

    private SQLiteDatabase databaseHexagramNote;

    public Database(Context context) {
        this.context = context;
    }

    public void openDatabase() {
        FileHelper.createFolder(CrossAppKey.DB_PATH_LIUYAO);
        super.database = this.openDatabase(CrossAppKey.DB_PATH_LIUYAO + "/" + CrossAppKey.DB_NAME_LIUYAO);
    }

    private SQLiteDatabase openDatabase(String databaseFile) {

        int resourceId = R.raw.database_structure_empty;
        InputStream is = this.context.getResources().openRawResource(
                resourceId);

        //加载64卦数据库
        int resourceIdHexagramNote = R.raw.sixty_four_hexagrams_note;
        InputStream isNote = this.context.getResources().openRawResource(resourceIdHexagramNote);
        databaseHexagramNote = super.openDatabase(CrossAppKey.DB_PATH_LIUYAO + "/" + CrossAppKey.DB_NAME_HEXAGRAM_NOTE, isNote);

        return super.openDatabase(databaseFile,is);
    }

    public void deleteHexagram(int id)
    {
        openDatabase();
        String[] args = {String.valueOf(id)};
        getDatabase().delete("Hexagram", "Id=?", args);
        closeDatabase();
    }

    public void insertHexagram(HexagramRow model)
    {
        openDatabase();
        ContentValues cv = new ContentValues();
        cv.put("OriginalName", model.getOriginalName());
        cv.put("ChangedName", model.getChangedName());
        cv.put("ShakeDate", model.getDate());
        cv.put("Note", model.getNote());
        getDatabase().insert("Hexagram", null, cv);
        closeDatabase();
    }

    HexagramRow createHexagramRowByCursor(Cursor cursor)
    {
        int idIndex = cursor.getColumnIndex("Id");
        int id = cursor.getInt(idIndex);

        String originalName = getColumnValue(cursor, "OriginalName");
        String changedName = getColumnValue(cursor,"ChangedName");
        String note = getColumnValue(cursor, "Note");

        String shakeDate = getColumnValue(cursor, "ShakeDate");

        HexagramRow hexagramRow = new HexagramRow(id,originalName,changedName,shakeDate, note);

        return hexagramRow;
    }

    public HexagramRow getHexagramById(int paramId)
    {
        openDatabase();
        String[] params = new String[]{ paramId+"" };
        String sql = "SELECT * FROM Hexagram where Id = ?";
        Cursor cur = database.rawQuery(sql, params);
        HexagramRow hexagram = null;
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            hexagram = createHexagramRowByCursor(cur);
            break;
        }
        closeDatabase();
        return hexagram;
    }

    public List<HexagramLineNote> getHexagramByNameAndLinePosition(String name)
    {
        openDatabase();
        String[] params = new String[]{ name };
        String sql = "SELECT * FROM HexagramsNote where Name = ?";
        Cursor cursor = databaseHexagramNote.rawQuery(sql, params);
        List<HexagramLineNote> hexagramLineNotes = new ArrayList<HexagramLineNote>();

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            HexagramLineNote hexagramLineNote = new HexagramLineNote();

            String hexagramName = getColumnValue(cursor, "Name");
            String linePosition = getColumnValue(cursor, "LinePosition");
            String noteType = getColumnValue(cursor, "NoteType");

            String originalNote = getColumnValue(cursor, "OriginalNote");
            String decoratedNote = getColumnValue(cursor, "DecoratedNote");

            hexagramLineNote.setName(hexagramName.trim());
            if(TextUtils.isEmpty(linePosition.trim()))
                hexagramLineNote.setPosition(8);
            else
                hexagramLineNote.setPosition(Integer.valueOf(linePosition));

            hexagramLineNote.setNoteType(noteType.trim());
            hexagramLineNote.setOriginalNote(originalNote.trim());
            hexagramLineNote.setDecoratedNote(decoratedNote.trim());

            hexagramLineNotes.add(hexagramLineNote);
        }
        closeDatabase();
        databaseHexagramNote.close();
        return hexagramLineNotes;
    }


    public void updateHexagram(HexagramRow hexagramRow) {

        openDatabase();
        ContentValues values = new ContentValues();
        values.put("Note", hexagramRow.getNote());
        String whereClause = "id=?";
        String[] whereArgs={String.valueOf(hexagramRow.getId())};
        database.update("Hexagram", values, whereClause, whereArgs);
        closeDatabase();
    }

    public ArrayList<HexagramRow> getHexagramList(String str){
        ArrayList<HexagramRow> list=new ArrayList<HexagramRow>();

        openDatabase();
        SQLiteDatabase database = super.getDatabase();

        String sqlCondition = " ";
        String[] params = new String[]{};
        if(!TextUtils.isEmpty(str)) {
            sqlCondition = " where (OriginalName = ? Or ShakeDate like ? Or Note like ?)";
            params = new String[]{ ""+str+"","%"+str+"%","%"+str+"%" };
        }

        String baseCondition = MyApplication.getInstance().getSearchCondition();
        if(!baseCondition.isEmpty())
        {
            DateExt dateExt = new DateExt();
            if(baseCondition.contains("{lastMonday}"))
            {
                baseCondition = baseCondition.replace("{lastMonday}", new DateExt(dateExt.getDate()).getLastWeekMonday().getFormatDateTime("yyyy-MM-dd"));
            }
            if(baseCondition.contains("{lastSunday}"))
            {
                baseCondition = baseCondition.replace("{lastSunday}", new DateExt(dateExt.getDate()).getLastWeekMonday().addDays(7).getFormatDateTime("yyyy-MM-dd"));
            }
            if(baseCondition.contains("{thisWeek}"))
            {
                baseCondition = baseCondition.replace("{thisWeek}",new DateExt(dateExt.getDate()).getThisWeekMonday().getFormatDateTime("yyyy-MM-dd"));
            }
            if(baseCondition.contains("{fromLastFriday}"))
            {
                baseCondition = baseCondition.replace("{fromLastFriday}",new DateExt(dateExt.getDate()).getThisWeekMonday().addDays(-3).getFormatDateTime("yyyy-MM-dd"));
            }
            sqlCondition = sqlCondition.replace("where","and");
        }

        String sql = "SELECT * FROM Hexagram "+ baseCondition + sqlCondition +" Order By ShakeDate DESC";

        Cursor cur = null;

        try
        {
            cur = database.rawQuery(sql, params);
        }
        catch (Exception ex)
        {
            sql = "SELECT * FROM Hexagram Order By ShakeDate DESC";
            cur = database.rawQuery(sql, new String[]{});
        }

        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {

            HexagramRow hexagramRow = createHexagramRowByCursor(cur);

            list.add(hexagramRow);
        }

        cur.close();
        closeDatabase();
        return list;
    }

    public void saveHexagramsToXML(List<HexagramRow> rows,  String localDir)
    {
        File file = new File(localDir);
        if (!file.exists()) {
            try {
                file.mkdirs();
            } catch (Exception e) {
                Log.d("XML saving folder", e.getMessage());
            }
        }

        boolean bFlag;
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd<HH:mm:ss>");
        String strTmpName = sDateFormat.format(new java.util.Date()) + ".xml";
        FileOutputStream fileos = null;

        File newXmlFile = new File(localDir + strTmpName);
        try {
            if (newXmlFile.exists()) {
                bFlag = newXmlFile.delete();
            } else {
                bFlag = true;
            }

            if (bFlag) {

                if (newXmlFile.createNewFile()) {

                    fileos = new FileOutputStream(newXmlFile);
                    XmlSerializer serializer = Xml.newSerializer();
                    serializer.setOutput(fileos, "UTF-8");
                    serializer.startDocument("UTF-8", null);

                    // start a tag called
                    serializer.startTag(null, "Hexagrams");
                    for (HexagramRow row : rows) {
                        serializer.startTag(null, "Hexagram");
                        serializer.attribute(null,"Id",String.valueOf(row.getId()));
                        serializer.attribute(null, "OriginalName", row.getOriginalName());
                        serializer.attribute(null, "ChangedName", row.getChangedName());
                        serializer.attribute(null, "ShakeDate", row.getDate());
                        serializer.attribute(null, "Note", row.getNote());
                        serializer.endTag(null, "Hexagram");
                    }
                    serializer.endTag(null, "Hexagrams");
                    serializer.endDocument();
                    serializer.flush();
                    fileos.close();
                    MediaScannerConnection.scanFile(MyApplication.getInstance(), new String[]{newXmlFile.getAbsolutePath()}, null, null);
                }
            }
        } catch (Exception e) {
            Log.d("member saving",e.getMessage());
        }
        //return bFlag;
    }

    public void importHexagramsToDb(List<HexagramRow> hexagramRows) {
        openDatabase();

        database.execSQL("delete from Hexagram");

        for (HexagramRow row: hexagramRows) {
            ContentValues cv = new ContentValues();
            cv.put("Id",row.getId());
            cv.put("OriginalName", row.getOriginalName());
            cv.put("ChangedName",row.getChangedName());
            cv.put("ShakeDate", row.getDate());
            cv.put("Note", row.getNote());
            database.insert("Hexagram", null, cv);
        }
        closeDatabase();
    }

    public void insertImageAttachment(List<ImageAttachment> models)
    {
        openDatabase();

        getDatabase().delete("ImageAttachment", "HexagramId ='" + models.get(0).getHexagramId() + "'", null);

        for(ImageAttachment imageAttachment : models) {
            ContentValues cv = new ContentValues();
            cv.put("HexagramId", imageAttachment.getHexagramId());
            cv.put("URL", imageAttachment.getUrl());
            getDatabase().insert("ImageAttachment", null, cv);
        }
        closeDatabase();
    }

    public List<ImageAttachment> getImageAttachmentByHexagramId(int hexagramId)
    {
        openDatabase();
        String[] params = new String[]{ hexagramId+"" };
        String sql = "SELECT * FROM ImageAttachment where HexagramId = ?";
        Cursor cur = database.rawQuery(sql, params);
        List<ImageAttachment> list = new ArrayList<ImageAttachment>();
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            ImageAttachment imageAttachment = createImageAttachmentByCursor(cur);
            //break;
            list.add(imageAttachment);
        }
        closeDatabase();
        return list;
    }

    ImageAttachment createImageAttachmentByCursor(Cursor cursor)
    {
        int id = getColumnIntValue(cursor,"Id");
        int hexagramId = getColumnIntValue(cursor, "HexagramId");
        String url = getColumnValue(cursor, "URL");

        ImageAttachment imageAttachment = new ImageAttachment();
        imageAttachment.setId(id);
        imageAttachment.setHexagramId(hexagramId);
        imageAttachment.setUrl(url);

        return imageAttachment;
    }

    public void deleteImageAttachmentByIds(int[] ids)
    {
        openDatabase();

        for(int i = 0 ; i < ids.length; i++) {
            getDatabase().delete("ImageAttachment", "Id ='" + ids[i] + "'", null);
        }

        closeDatabase();
    }
    public void deleteImageAttachmentByHexagramRowId(int id)
    {
        openDatabase();
        getDatabase().delete("ImageAttachment", "HexagramId ='" + id + "'", null);
        closeDatabase();
    }

    public void deleteFutureDataByHexagramRowId(int id)
    {
        openDatabase();
        getDatabase().delete("FutureData", "HexagramId ='" + id + "'", null);
        closeDatabase();
    }

    public void insertFutureData(List<DailyData> models)
    {
        openDatabase();

        getDatabase().delete("FutureData", "HexagramId ='" + models.get(0).HexagramId + "'", null);

        for(DailyData dailyData : models) {
            ContentValues cv = new ContentValues();
            cv.put("HexagramId", dailyData.HexagramId);
            cv.put("FutureCode", dailyData.FutureCode);
            cv.put("Date", dailyData.DateTime);
            cv.put("OpeningPrice", dailyData.OpeningPrice);
            cv.put("ClosingPrice", dailyData.ClosingPrice);
            cv.put("HighestPrice", dailyData.HighestPrice);
            cv.put("LowestPrice", dailyData.LowestPrice);
            cv.put("Volume", dailyData.Volume);
            getDatabase().insert("FutureData", null, cv);
        }
        closeDatabase();
    }

    public List<DailyData> getDailyDataByHexagramId(int hexagramId)
    {
        openDatabase();
        String[] params = new String[]{ hexagramId+"" };
        String sql = "SELECT * FROM FutureData where HexagramId = ?";
        Cursor cur = database.rawQuery(sql, params);
        List<DailyData> list = new ArrayList<DailyData>();
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            DailyData dailyData = createDailyDataByCursor(cur);
            //break;
            list.add(dailyData);
        }
        closeDatabase();
        return list;
    }

    @Override
    public void closeDatabase()
    {
        super.closeDatabase();
        databaseHexagramNote.close();
    }

    DailyData createDailyDataByCursor(Cursor cursor)
    {
        int id = getColumnIntValue(cursor,"Id");
        int hexagramId = getColumnIntValue(cursor, "HexagramId");
        String futureCode = getColumnValue(cursor, "FutureCode");
        String date = getColumnValue(cursor, "Date");
        double openingPrice = getColumnDoubleValue(cursor, "OpeningPrice");
        double highestPrice = getColumnDoubleValue(cursor, "HighestPrice");
        double lowestPrice = getColumnDoubleValue(cursor, "LowestPrice");
        double closingPrice = getColumnDoubleValue(cursor, "ClosingPrice");
        int volume = getColumnIntValue(cursor, "Volume");

        DailyData dailyData = new DailyData();
        dailyData.Id = id;
        dailyData.HexagramId = hexagramId;
        dailyData.FutureCode = futureCode;
        dailyData.DateTime = date;
        dailyData.OpeningPrice = openingPrice;
        dailyData.ClosingPrice =closingPrice;
        dailyData.HighestPrice = highestPrice;
        dailyData.LowestPrice = lowestPrice;

        return dailyData;
    }
}
