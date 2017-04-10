package com.vsu.nastya.partymanager.logic;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;

import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.item_list.data.Item;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;
import com.vsu.nastya.partymanager.party_list.Party;

import java.util.ArrayList;

import static com.vsu.nastya.partymanager.party_details.PartyDetailsPagerAdapter.*;

/**
 * Класс для работы с уведомлениями.
 */

public abstract class Notifications {

    private static final String CURRENT_TAB = "current_tab";
    private static final String PARTY = "party";

    /**
     * Оповещение о том, что на встречу приглашен новый гость.
     * @param currentParty Встреча, на которую приглашен гость.
     */
    public static void newGuestAdded(Context context, Party currentParty) {
        ArrayList<Guest> guests = currentParty.getGuests();
        String title = context.getResources().getString(R.string.new_friend_in) +
                " \"" + currentParty.getName() + "\":";
        String text = guests.get(guests.size() - 1).getGuestName();

        Builder builder = new Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_group_white_24dp);

        Bundle bundle = new Bundle();
        bundle.putInt(CURRENT_TAB, GUESTS_TAB);
        bundle.putSerializable(PARTY, currentParty);
        builder = setUpSimpleNotificationAction(context, PartyDetailsActivity.class, builder, bundle);

        sendNotification(context, builder);
    }

    /**
     * Оповещение о том, что в список покупок добавлена новая позиция.
     * @param currentParty Встреча, в список которой добавлена позиция.
     */
    public static void newItemAdded(Context context, Party currentParty) {
        ArrayList<Item> items = currentParty.getItems();
        Item item = items.get(items.size() - 1);
        String title = context.getResources().getString(R.string.new_item_in) +
                " \"" + currentParty.getName() + "\":";
        String text = item.getName() + " (" + item.getQuantity() + ")";

        Builder builder = new Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_shopping_cart_white_24dp);

        Bundle bundle = new Bundle();
        bundle.putInt(CURRENT_TAB, ITEMS_TAB);
        bundle.putSerializable(PARTY, currentParty);
        builder = setUpSimpleNotificationAction(context, PartyDetailsActivity.class, builder, bundle);

        sendNotification(context, builder);
    }

    /**
     * Оповещение о том, что установлено новое место проведения встречи.
     * @param currentParty Встреча, место проведения которой изменено.
     */
    public static void newLocationSet(Context context, Party currentParty) {
        String title = context.getResources().getString(R.string.new_location_set) +
                " \"" + currentParty.getName() + "\":";
        String text = currentParty.getPlace();

        Builder builder = new Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_pin_drop_white_24dp);

        Bundle bundle = new Bundle();
        bundle.putInt(CURRENT_TAB, LOCATION_TAB);
        bundle.putSerializable(PARTY, currentParty);
        builder = setUpSimpleNotificationAction(context, PartyDetailsActivity.class, builder, bundle);

        sendNotification(context, builder);
    }

    /**
     * Настройка оповещения: создание искусственного стека, вызов соответсвующего фрагмента
     * по клику на оповещение, закрытие оповещения.
     * @param to Fragment, на который перенаправляется пользователь по клику на оповещение.
     * @param bundle передает данные о вечеринке и о том какую вкладу во ViewPager открыть.
     */
    private static Builder setUpSimpleNotificationAction(Context context, Class<?> to, Builder builder, Bundle bundle) {
        Intent notificationIntent = new Intent(context, to);
        notificationIntent.putExtra(PARTY, bundle.getSerializable(PARTY));
        notificationIntent.putExtra(CURRENT_TAB, bundle.getInt(CURRENT_TAB));

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PartyDetailsActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        return builder;
    }

    private static void sendNotification(Context context, Builder builder) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
