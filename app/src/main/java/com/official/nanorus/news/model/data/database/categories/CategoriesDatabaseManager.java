package com.official.nanorus.news.model.data.database.categories;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.official.nanorus.news.entity.data.categories.Category;
import com.official.nanorus.news.model.data.Utils;
import com.official.nanorus.news.model.data.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

public class CategoriesDatabaseManager {

    private final String TAG = this.getClass().getSimpleName();

    private static CategoriesDatabaseManager instance;
    private DatabaseHelper databaseHelper;
    private CategoriesDatabaseContract databaseContract;

    public static CategoriesDatabaseManager getInstance() {
        if (instance == null)
            instance = new CategoriesDatabaseManager();
        return instance;
    }

    public CategoriesDatabaseManager() {
        databaseHelper = DatabaseHelper.getInstance();
        databaseContract = new CategoriesDatabaseContract();
    }


    public Single<List<Category>> getCategoriesList() {
        return Single.create(emitter -> {
            SQLiteDatabase db = databaseHelper.getReadableDB();
            Cursor cursor = db.rawQuery("SELECT * FROM " + databaseContract.TABLE_NAME_CATEGORIES
                    + " WHERE " + databaseContract.COLUMN_NAME_CATEGORY_LANG + " = '" + Utils.getAppLanguage() + "'"
                    , null);
            List<Category> categoriesList = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    String image = cursor.getString(cursor.getColumnIndex(databaseContract.COLUMN_NAME_CATEGORY_IMAGE));
                    categoriesList.add(new Category(
                                    cursor.getInt(cursor.getColumnIndex(databaseContract.COLUMN_NAME_ID)),
                                    cursor.getString(cursor.getColumnIndex(databaseContract.COLUMN_NAME_CATEGORY_NAME)),
                                    cursor.getString(cursor.getColumnIndex(databaseContract.COLUMN_NAME_CATEGORY_DEFAULT_NAME)),
                                    image
                            )
                    );
                } while (cursor.moveToNext());
                emitter.onSuccess(categoriesList);
                cursor.close();
                databaseHelper.closeDB();
            }
        });
    }

    public int putCategories(List<Category> categories) {
        SQLiteDatabase db = databaseHelper.getWritableDB();
        ContentValues cv = new ContentValues();

        for (Category category : categories) {
            cv.put(databaseContract.COLUMN_NAME_ID, category.getId());
            if (category.getName() != null) {
                cv.put(databaseContract.COLUMN_NAME_CATEGORY_NAME, category.getName());
            }
            cv.put(databaseContract.COLUMN_NAME_CATEGORY_IMAGE, String.valueOf(category.getImage()));

            if (cv.size() > 0) {
                db.insert(databaseContract.TABLE_NAME_CATEGORIES, null, cv);
                cv.clear();
            }
        }
        databaseHelper.closeDB();
        return 0;
    }

    public int clearCategories() {
        SQLiteDatabase db = databaseHelper.getWritableDB();
        db.delete(databaseContract.TABLE_NAME_CATEGORIES, null, null);
        databaseHelper.closeDB();
        return 0;
    }

    public Single<Category> getCategory(int categoryId) {
        return Single.create(emitter -> {
            SQLiteDatabase db = databaseHelper.getReadableDB();
            Cursor cursor = db.rawQuery("SELECT * FROM " + databaseContract.TABLE_NAME_CATEGORIES + " WHERE "
                    + databaseContract.COLUMN_NAME_ID + " = " + categoryId + " AND "
                            + databaseContract.COLUMN_NAME_CATEGORY_LANG + " = '" + Utils.getAppLanguage() + "'"
                    , null);
            if (cursor.moveToFirst()) {
                String image = cursor.getString(cursor.getColumnIndex(databaseContract.COLUMN_NAME_CATEGORY_IMAGE));
                Category category = new Category(
                        cursor.getInt(cursor.getColumnIndex(databaseContract.COLUMN_NAME_ID)),
                        cursor.getString(cursor.getColumnIndex(databaseContract.COLUMN_NAME_CATEGORY_NAME)),
                        cursor.getString(cursor.getColumnIndex(databaseContract.COLUMN_NAME_CATEGORY_DEFAULT_NAME)),
                        image
                );
                emitter.onSuccess(category);
                cursor.close();
                databaseHelper.closeDB();
            }
        });
    }

    public void insertDefaultCategories() {
        SQLiteDatabase db = databaseHelper.getWritableDB();
        db.execSQL(databaseContract.SQL_FIRST_INSERT_CATEGORIES);
    }
}
