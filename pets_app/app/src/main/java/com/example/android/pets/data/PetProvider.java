package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class PetProvider extends ContentProvider {

    private static final String LOG_TAG = PetProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS,PETS);

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS+"/#",PET_ID);
    }

    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * @param strings represents projection
     * @param s represents selection
     * @param strings1 represents selectionArgs
     * @param s1 represents sortOrder
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match){
            case PETS:
                cursor = database.query(PetContract.PetEntry.TABLE_NAME,strings,s,strings1,null,null,s1);
                break;

            case PET_ID:
                s = PetContract.PetEntry._ID + "=?";
                //ContentUris.parseId(uri) returns the trailing integer in the content uri which replaces ?
                //in the selection variable
                strings1 = new String[]{ String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(PetContract.PetEntry.TABLE_NAME,strings,s,strings1,null,null,s1);
                break;

            default:
                throw new IllegalArgumentException("cannot query unknown URI " + uri);

        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + "with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return insertPet(uri,contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues contentValues) {

        //Sanity check
        //Data is valid or not
        String name = contentValues.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null){
            throw new IllegalArgumentException("Pet requires a name.");

        }

        Integer gender = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetContract.PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet requires valid gender.");
        }

        Integer weight = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0){
            throw new IllegalArgumentException("Pet requires a weight.");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(PetContract.PetEntry.TABLE_NAME,null,contentValues);

        if (id == -1){
            Log.e(LOG_TAG,"Failed to insert row for  " + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri,id);
    }

    /**
     * @param s represents selection
     * @param strings represents selectionArgs
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return database.delete(PetContract.PetEntry.TABLE_NAME,s,strings);
            case PET_ID:
                s = PetContract.PetEntry._ID + "=?";
                strings = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(PetContract.PetEntry.TABLE_NAME,s,strings);
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    /**
     * @param s represents selection
     * @param strings represents selectionArgs
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return updatePet(uri, contentValues, s, strings);

            case PET_ID:
                s = PetContract.PetEntry._ID + "=?";
                strings = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri,contentValues,s,strings);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);

        }
    }

    private int updatePet(Uri uri, ContentValues contentValues, String s, String[] strings) {

        if (contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)){
            String name = contentValues.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if (name == null){
                throw new IllegalArgumentException("Pet requires a name.");
            }
        }

        if (contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)){
            Integer gender = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetContract.PetEntry.isValidGender(gender)){
                throw new IllegalArgumentException("Pet requires valid gender.");
            }
        }

        if (contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)){
            Integer weight = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight <0){
                throw new IllegalArgumentException("Pet requires valid weight.");
            }
        }

        if (contentValues.size() == 0){
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        return database.update(PetContract.PetEntry.TABLE_NAME,contentValues,s,strings);
    }
}
