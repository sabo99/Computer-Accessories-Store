package com.alvin.computeraccessoriesstore.Common;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.TextView;

import com.alvin.computeraccessoriesstore.Model.ItemsModel;
import com.alvin.computeraccessoriesstore.Model.StoreModel;
import com.alvin.computeraccessoriesstore.Model.UserModel;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

public class Common {
    public static final String USER_REF = "User-CAS";
    public static final String STORE_REF = "ACS-Store";
    public static final String ORDER_REF = "Order";
    public static final String SLIDER_REF = "Slider";
    public static final String F_NAME = "name";
    public static final String F_EMAIL = "email";
    public static final String F_PHONE = "phone";
    public static final String F_PASS = "password";
    public static final String F_STATUS = "status";
    //public static final String F_IMAGE = "image";
    public static final String F_ID = "id";
    public static final String CHILD_ITEMS = "items";

    public static final int REQUEST_WRITE_PERMISSION_GALLERY = 78905;
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;

    public static UserModel currentUser;
    public static StoreModel storeItemsSelected;
    public static ItemsModel selectedItems;

    public static String formatPrice(double price) {
        if (price != 0)
        {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(df.format(price)).toString();
            return finalPrice;
        }
        else
            return "0,00";
    }

    public static String createOrderNumber() {
        return new StringBuilder()
                .append(System.currentTimeMillis())         // Get current time in millisecond
                .append(Math.abs(new Random().nextInt()))   // Add random number to block same order at same time
                .toString();
    }

    public static String getDateOfWeek(int i) {
        switch (i)
        {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
            default:
                return "Unknown";
        }
    }

    public static String convertStatusToText(int orderStatus) {
        switch (orderStatus)
        {
            case 0:
                return "Cancelled";
            case 1:
                return "Ordered";
            default:
                return "Unknown";
        }
    }

    public static void setSpanString(String welcome, String name, TextView textView) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }
}
