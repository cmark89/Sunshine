package com.objectivelyradical.sunshine;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.objectivelyradical.sunshine.Data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_DEFAULT = 1;
    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_DEFAULT;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        String highLowStr = Utility.convertTempToSystemUnit(high, mContext) + "/" +
                Utility.convertTempToSystemUnit(low, mContext);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layout = -1;    // why -1?
        if(viewType == VIEW_TYPE_TODAY) {
            layout = R.layout.list_item_forecast_today;
        }
        else {
            layout = R.layout.list_item_forecast;
        }
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        view.setTag(new ViewHolder(view));

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder)view.getTag();

        TextView dateView = holder.dateView;
        TextView highView = holder.highView;
        TextView lowView = holder.lowView;
        TextView weatherView = holder.descriptionView;

        dateView.setText(Utility.getFriendlyDayString(mContext, cursor.getLong
                (ForecastFragment.COL_WEATHER_DATE)));
        highView.setText(Utility.convertTempToSystemUnit(cursor.getInt
                (ForecastFragment.COL_WEATHER_MAX_TEMP), mContext));
        lowView.setText(Utility.convertTempToSystemUnit(cursor.getInt
                (ForecastFragment.COL_WEATHER_MIN_TEMP), mContext));
        weatherView.setText(cursor.getString(ForecastFragment.COL_WEATHER_DESC));

        //TextView tv = (TextView)view;
        //tv.setText(convertCursorRowToUXFormat(cursor));
    }

    public static class ViewHolder {
        ImageView iconView;
        TextView dateView;
        TextView descriptionView;
        TextView highView;
        TextView lowView;

        public ViewHolder(View view) {
            iconView = (ImageView)view.findViewById(R.id.list_item_icon);
            dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
            highView = (TextView)view.findViewById(R.id.list_item_high_textview);
            lowView = (TextView)view.findViewById(R.id.list_item_low_textview);
        }
    }
}