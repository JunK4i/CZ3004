package com.example.myapplication.Adapters;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Directions;
import com.example.myapplication.Entities.ArenaGame1;
import com.example.myapplication.Entities.Cell;
import com.example.myapplication.Helpers.ImageMapper;
import com.example.myapplication.R;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

/**
 * Custom Recycler Adapter for gridRecycleView. It connects the data from xx to the view xx
 * Recycle View Tutorial https://guides.codepath.com/android/using-the-recyclerview
 */

public class GridRecyclerAdapter extends RecyclerView.Adapter<GridRecyclerAdapter.TileViewHolder>{
    private MutableLiveData<ArenaGame1> liveGame1;
    private ArenaGame1 game1;
    private List<Cell> cells;
    private ImageMapper imageMapper;
    private Context context;


    public GridRecyclerAdapter(MutableLiveData<ArenaGame1> liveGame1, LifecycleOwner owner) {
        this.liveGame1 = liveGame1;
        game1 = liveGame1.getValue();
        context = game1.getContext();
        cells = game1.getArenaGrid().getCells();
        // When livedata changes, update game state in adapter and notifyDataSetChanged
        liveGame1.observe(owner, liveGame1Instance->{
            game1 = liveGame1Instance;
            cells = game1.getArenaGrid().getCells();
            notifyDataSetChanged();
        });
    }
    @NonNull
    @Override
    public TileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        imageMapper = new ImageMapper(parent.getContext());
        View itemCell = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cell, parent, false); // inflate the viewholder with item_cell layout
        return new TileViewHolder(itemCell);
    }

    @Override
    public void onBindViewHolder(@NonNull TileViewHolder holder, int position) {
        holder.bind(cells.get(position));
        holder.setIsRecyclable(false); // Item is not recyclable
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    // nested class TilerViewHolder
    class TileViewHolder extends RecyclerView.ViewHolder{
        TextView cellTextView;
        View itemCell;
        int imageResource;
        int position;


        public TileViewHolder(@NonNull View itemCell) {
            super(itemCell);
            cellTextView = itemCell.findViewById(R.id.item_cell_textView);
            this.itemCell = itemCell;
        }

        // Bind to each view
        // Update the cell's properties and visuals according to the information linked to that cell.
        // Each change to itemCell or cellTextView should call invalidate() method to ensure that the cell is updated
        public void bind(final Cell cell) {
            position = this.getLayoutPosition();
            imageResource = imageMapper.getImageResource(cell.getImageId());
            itemCell.setBackgroundResource(imageResource);
            itemCell.setTag("obstacleFromCell:"+cell.getX()+","+cell.getY()); // Tag data is used to identify sourceType and destination upon drop
            cellTextView = itemCell.findViewById(R.id.item_cell_textView);

            if(cell.isRobot()){ // robot direction of image
                switch (cell.getDirection()){
                    case NORTH: itemCell.setRotation(0);break;
                    case EAST: itemCell.setRotation(90);break;
                    case SOUTH: itemCell.setRotation(180);break;
                    case WEST: itemCell.setRotation(270);break;
                    default: break;
                }
            }

            if(cell.isObstacle()){ // object, add direction marker
                if(cell.getDirection().equals(Directions.NORTH)){
                    cellTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.up, 0, 0);
                }else if (cell.getDirection().equals(Directions.EAST)){
                    cellTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.right, 0);
                }else if (cell.getDirection().equals(Directions.SOUTH)){
                    cellTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.down);
                }else if (cell.getDirection().equals(Directions.WEST)){
                    cellTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.left, 0, 0, 0);
                }
                if(!cell.isRevealed()){ // hidden object
                    cellTextView.setText(""+cell.getObstacleId());
                    cellTextView.setTextColor(Color.WHITE);
                }
                cellTextView.invalidate();
            }

            itemCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cell.getType().equals(Cell.Types.OBSTACLE)){
                        cell.rotateDirection();
                        liveGame1.setValue(game1);
                    }
                }
            });

            // Listener to initialise drag
            itemCell.setOnLongClickListener(v->{
                Log.d("itemCell", ""+v.getTag());
                ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
                ClipData dragData = new ClipData(
                        (CharSequence) v.getTag(),
                        new String[] {ClipDescription.MIMETYPE_TEXT_PLAIN},
                        item);

                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(itemCell);
                    v.startDragAndDrop(
                            dragData,  // The data to be dragged
                            myShadow,  // The drag shadow builder
                            this,
                            0);
                    return true;
            });

            // Listener to respond to drag event
            itemCell.setOnDragListener(new View.OnDragListener() {
                @Override
                // view refers to the view in which the event has triggered. It is the same view as itemCell.
                public boolean onDrag(View view, DragEvent event) {
                    switch (event.getAction()){
                        case DragEvent.ACTION_DRAG_STARTED: // A drag event has started
                            if(event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){
                                return true;
                            }
                            return false;
                        case DragEvent.ACTION_DRAG_ENTERED: // Enters bounding box of listener cell
                            cellTextView.setBackgroundColor(Color.RED);
                            cellTextView.invalidate();
                            showToastMessage("["+ cell.getX()+","+cell.getY()+"]",600);
                            return true;
                        case DragEvent.ACTION_DRAG_LOCATION: // Subsequent to action_drag_entered, still inside
                            return true;
                        case DragEvent.ACTION_DRAG_EXITED: // Move outside bounding box
                            cellTextView.setBackgroundColor(Color.TRANSPARENT);
                            cellTextView.invalidate();
                            return true;
                        case DragEvent.ACTION_DROP: // Item has dropped, handle data transfer
                            ClipData.Item item = event.getClipData().getItemAt(0); // Gets the item containing the dragged data.
                            CharSequence dragData = item.getText(); // Gets the tag from the item.
                            // Tag format: sourceType:X,Y
                            Pattern colon = Pattern.compile(":");
                            String[] check = colon.split(dragData);
                            if(cell.isEmpty()){ // only handle if the destination cell is empty
                                if(check[0].equals("obstacleFromCell")){
                                    if(check[1].equals(""+cell.getX()+","+cell.getY())){ // same cell
                                        return false;
                                    }
                                    Pattern comma = Pattern.compile(",");
                                    String[] coords = comma.split(check[1]);
                                    // Get source coords
                                    Integer sourceX = Integer.parseInt(coords[0]);
                                    Integer sourceY = Integer.parseInt(coords[1]);
                                    // Update game
                                    Log.d("dragDrop", "fromCell "+sourceX+","+sourceY +" onto "+cell.getX()+","+cell.getY()+","+ArenaGame1.dirToInt(cell.getDirection()));
                                    game1.getArenaGrid().moveObstacle(sourceX,sourceY, cell.getX(),cell.getY());
                                    liveGame1.setValue(game1);
                                    itemCell.invalidate();
                                    showToastMessage("Set ["+ cell.getX()+","+cell.getY()+"]",1000);
                                } else if(check[0].equals("obstacleFromFragment")) {
                                    // Update game
                                    Log.d("dragDrop", "fromFragment on "+cell.getX()+","+cell.getY());
                                    game1.getArenaGrid().spawnObstacle(cell.getX(),cell.getY());
                                    liveGame1.setValue(game1);
                                    itemCell.invalidate();
                                    showToastMessage("Set ["+ cell.getX()+","+cell.getY()+"]",1000);
                                } else{return false;}
                                return true;
                            } else{return false;}
                        case DragEvent.ACTION_DRAG_ENDED:
                            if (event.getResult()) {
                                Log.d("dragEnd","success");
                            } else {
                                Log.d("dragEnd","failure");
                                cellTextView.setBackgroundColor(Color.TRANSPARENT);
                                cellTextView.invalidate();
                            }
                            return true;
                        default: // An unknown action type was received.
                            Log.e("DragDrop Example","Unknown action type received by View.OnDragListener.");
                            break;
                    }
                    return false;
                }
            });
        }
    }

    // Show toast helper function
    public void showToastMessage(String text, int duration){
        final Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }
}
