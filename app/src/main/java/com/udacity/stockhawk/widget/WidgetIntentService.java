package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.util.Locale;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetIntentService extends RemoteViewsService {

    private static final String[] STOCK_COLUMNS = {
            Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE
    };

    static final int INDEX_ID = 0;
    private static final int INDEX_SYMBOL = 1;
    private static final int INDEX_PRICE = 2;
    private static final int INDEX_ABSOLUTE_CHANGE = 3;
    private static final int INDEX_PERCENTAGE_CHANGE = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(Contract.Quote.URI, STOCK_COLUMNS, null,
                        null, null);
                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                Locale current = Locale.getDefault();
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.list_item_quote);
                String stockName = data.getString(INDEX_SYMBOL);
                double stockPrice = data.getDouble(INDEX_PRICE);
                double stockChange;
                String displayModeTypeAbsolute = "";
                String displayModeTypePercentage = "";
                if (PrefUtils.getDisplayMode(getBaseContext()).equals(getBaseContext().getString(R.string.pref_display_mode_absolute_key))) {
                    stockChange = data.getDouble(INDEX_ABSOLUTE_CHANGE);
                    displayModeTypeAbsolute = "$";

                } else {
                    stockChange = data.getDouble(INDEX_PERCENTAGE_CHANGE);
                    displayModeTypePercentage = "%";
                }
                String stockChangeString = String.valueOf(String.format(current, "%.2f", stockChange));
                String modifier;

                views.setTextViewText(R.id.symbol, stockName);
                views.setTextViewText(R.id.price, "$" + String.valueOf(String.format(current, "%.2f", stockPrice)));
                if (stockChangeString.contains("-")) {
                    stockChangeString = stockChangeString.replace("-", "");
                    modifier = "-";
                    views.setInt(R.id.change, "setBackgroundResource",
                            R.drawable.percent_change_pill_red);
                } else {
                    modifier = "+";
                }
                views.setTextViewText(R.id.change, modifier + displayModeTypeAbsolute + stockChangeString + displayModeTypePercentage);


                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
