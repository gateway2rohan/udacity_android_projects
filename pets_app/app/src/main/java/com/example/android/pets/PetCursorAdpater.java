package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;

import org.w3c.dom.Text;

public class PetCursorAdpater extends CursorAdapter {
    public PetCursorAdpater(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,viewGroup,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView)view.findViewById(R.id.name);
        TextView summaryTextView = (TextView)view.findViewById(R.id.summary);

        int nameColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
        int summaryColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);


        String petName = cursor.getString(nameColumnIndex);
        String petBreed = cursor.getString(summaryColumnIndex);

        nameTextView.setText(petName);
        summaryTextView.setText(petBreed);
    }
}
