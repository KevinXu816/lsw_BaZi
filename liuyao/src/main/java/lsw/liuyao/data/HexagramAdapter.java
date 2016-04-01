package lsw.liuyao.data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lsw.hexagram.LineComparator;
import lsw.library.ColorHelper;
import lsw.liuyao.HexagramAnalyzerActivity;
import lsw.liuyao.R;
import lsw.liuyao.model.HexagramLineNote;
import lsw.model.EnumLineSymbol;
import lsw.model.Hexagram;
import lsw.model.Line;

/**
 * Created by swli on 8/7/2015.
 */
public class HexagramAdapter extends BaseAdapter {

    private ArrayList<Line> lines;
    private Hexagram hexagram;
    private LayoutInflater layoutInflater;
    private Context context;
    private boolean isChangedHexagram;
    private ColorHelper colorHelper;

    private HashMap<Integer,String> sixAnimals;
    public void setSixAnimals(HashMap<Integer,String> sixAnimals)
    {
        this.sixAnimals = sixAnimals;
    }
    private HashMap<Integer,String> getSixAnimals()
    {
        return this.sixAnimals;
    }

    private List<HexagramLineNote> lineNotes;
    private Database database;

    public HexagramAdapter(Hexagram hexagram, Context context)
    {
        this(hexagram,context,false);
    }

    public HexagramAdapter(Hexagram hexagram, Context context, boolean isChangedHexagram)
    {
        this.hexagram = hexagram;
        this.context = context;
        this.lines = hexagram.getLines();
        Collections.sort(this.lines,new LineComparator());
        this.layoutInflater = LayoutInflater.from(context);
        this.isChangedHexagram = isChangedHexagram;
        colorHelper = ColorHelper.getInstance(context);

        database = new Database(context);
        lineNotes = database.getHexagramByNameAndLinePosition(hexagram.getName());
    }

    @Override
    public int getCount() {
        return lines.size();
    }

    @Override
    public Object getItem(int i) {
        return lines.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Controls controls;
        if (view == null) {
            controls = new Controls();
            //view = layoutInflater.inflate(R.layout.member_row,null);

            view = layoutInflater.inflate(R.layout.line_item, null);
            controls.tvAttachedLine = (TextView) view.findViewById(R.id.tvAttachedLine);
            controls.ivLineSymbol = (ImageView) view.findViewById(R.id.ivLineSymbol);
            controls.tvLine = (TextView) view.findViewById(R.id.tvLine);
            controls.tvSelfTarget = (TextView)view.findViewById(R.id.tvSelfOrTarget);
            controls.tvSixAnimal = (TextView)view.findViewById(R.id.tvSixAnimal);

            view.setTag(controls);
        } else {
            controls = (Controls) view.getTag();
        }
        final Line line = lines.get(i);
        //String lineText= line.getSixRelation().toString() +" " + line.getEarthlyBranch().getName() + " " + line.getEarthlyBranch().getFiveElement().toString();
        //controls.tvLine.setText(lineText);
        controls.tvLine.setText("");
        SpannableString ssSixRelation = ColorHelper.getTextByColor(line.getSixRelation().toString(), Color.GRAY);
        SpannableString ssName = colorHelper.getColorTerrestrial(line.getEarthlyBranch().getName());
        SpannableString ssFiveElement = ColorHelper.getTextByColor(line.getEarthlyBranch().getFiveElement().toString(),Color.GRAY);
        controls.tvLine.append(ssSixRelation);
        controls.tvLine.append(" ");
        controls.tvLine.append(ssName);
        controls.tvLine.append(" ");
        controls.tvLine.append(ssFiveElement);

        if(!isChangedHexagram) {

            String self = hexagram.getSelf() == line.getPosition() ? "世" : "";
            String target = hexagram.getTarget() == line.getPosition() ? "应" : "";
            controls.tvSelfTarget.setText(self+target);

            if (line.getEarthlyBranchAttached() != null) {
                controls.tvAttachedLine.setText("");
                SpannableString ssSixRelationAttached = ColorHelper.getTextByColor(line.getSixRelationAttached().toString(), Color.GRAY);
                SpannableString ssNameAttached = colorHelper.getColorTerrestrial(line.getEarthlyBranchAttached().getName());
                SpannableString ssFiveElementAttached = ColorHelper.getTextByColor(line.getEarthlyBranchAttached().getFiveElement().toString(),Color.GRAY);
                controls.tvAttachedLine.append(ssSixRelationAttached);
                controls.tvAttachedLine.append(" ");
                controls.tvAttachedLine.append(ssNameAttached);
                controls.tvAttachedLine.append(" ");
                controls.tvAttachedLine.append(ssFiveElementAttached);
            } else {
                controls.tvAttachedLine.setText("");
            }
        }
        else {
            controls.tvSelfTarget.setText("");
            controls.tvAttachedLine.setVisibility(View.GONE);
        }

        if(getSixAnimals() == null)
            controls.tvSixAnimal.setText("");
        else
        {
            controls.tvSixAnimal.setText(getSixAnimals().get(line.getPosition()));
        }

        int resourceId = R.drawable.yang;
        final EnumLineSymbol lineSymbol = line.getLineSymbol();
        if(lineSymbol.equals(EnumLineSymbol.LaoYang))
            resourceId = R.drawable.laoyang;
        else if(lineSymbol.equals(EnumLineSymbol.LaoYin))
            resourceId = R.drawable.laoyin;
        else if(lineSymbol.equals(EnumLineSymbol.Yin))
            resourceId = R.drawable.yin;
        controls.ivLineSymbol.setImageResource(resourceId);

        final HashMap<Integer,String> mappingPosition = new HashMap<Integer, String>();
        mappingPosition.put(2,"二");
        mappingPosition.put(3,"三");
        mappingPosition.put(4,"四");
        mappingPosition.put(5,"五");

        final int linePosition = 6 - i;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String noteTuanCi = "";
                String noteXiangCi = "";
                for(HexagramLineNote lineNote : lineNotes )
                {
                    if(lineNote.getPosition() == linePosition)
                    {
                        if(lineNote.getNoteType().trim().equals("彖"))
                        {
                            noteTuanCi = "彖词--------------------\n原文: " + lineNote.getOriginalNote() + "\n译文: "+ lineNote.getDecoratedNote();
                        }
                        else
                        {
                            noteXiangCi = "象词--------------------\n原文: " + lineNote.getOriginalNote() + "\n译文: " + lineNote.getDecoratedNote();
                        }
                    }
                }

                String title = linePosition + "";

                if (lineSymbol == EnumLineSymbol.LaoYang || lineSymbol == EnumLineSymbol.Yang)
                    title = "九";
                else
                    title = "六";

                if(linePosition == 1)
                    title = "初" + title;
                else if(linePosition == 6)
                    title = "上" + title;
                else
                {
                    title = title + mappingPosition.get(linePosition);
                }
                    new AlertDialog.Builder(context)
                            .setTitle(title)
                            .setMessage(noteTuanCi + "\n" + noteXiangCi)
                            .show();
            }
        });

        return view;
    }

    public final class Controls
    {
        public TextView tvAttachedLine;
        public ImageView ivLineSymbol;
        public TextView tvLine;
        public TextView tvSelfTarget;
        public TextView tvSixAnimal;
    }
}
