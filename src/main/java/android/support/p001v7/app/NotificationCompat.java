package android.support.p001v7.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.p000v4.app.BundleCompat;
import android.support.p000v4.app.NotificationBuilderWithBuilderAccessor;
import android.support.p000v4.app.NotificationCompat.Builder;
import android.support.p000v4.app.NotificationCompat.BuilderExtender;
import android.support.p000v4.app.NotificationCompat.MessagingStyle;
import android.support.p000v4.app.NotificationCompat.MessagingStyle.Message;
import android.support.p000v4.app.NotificationCompat.Style;
import android.support.p000v4.media.session.MediaSessionCompat.Token;
import android.support.p000v4.text.BidiFormatter;
import android.support.p000v4.view.ViewCompat;
import android.support.p001v7.appcompat.C0082R;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.widget.RemoteViews;
import java.util.List;

/* renamed from: android.support.v7.app.NotificationCompat */
public class NotificationCompat extends android.support.p000v4.app.NotificationCompat {

    /* renamed from: android.support.v7.app.NotificationCompat$Api24Extender */
    private static class Api24Extender extends BuilderExtender {
        private Api24Extender() {
        }

        public Notification build(Builder b, NotificationBuilderWithBuilderAccessor builder) {
            NotificationCompat.addStyleToBuilderApi24(builder, b);
            return builder.build();
        }
    }

    /* renamed from: android.support.v7.app.NotificationCompat$Builder */
    public static class Builder extends android.support.p000v4.app.NotificationCompat.Builder {
        public Builder(Context context) {
            super(context);
        }

        @RestrictTo({Scope.GROUP_ID})
        protected CharSequence resolveText() {
            if (this.mStyle instanceof MessagingStyle) {
                MessagingStyle style = this.mStyle;
                Message m = NotificationCompat.findLatestIncomingMessage(style);
                CharSequence conversationTitle = style.getConversationTitle();
                if (m != null) {
                    if (conversationTitle != null) {
                        return NotificationCompat.makeMessageLine(this, style, m);
                    }
                    return m.getText();
                }
            }
            return super.resolveText();
        }

        @RestrictTo({Scope.GROUP_ID})
        protected CharSequence resolveTitle() {
            if (this.mStyle instanceof MessagingStyle) {
                MessagingStyle style = this.mStyle;
                Message m = NotificationCompat.findLatestIncomingMessage(style);
                CharSequence conversationTitle = style.getConversationTitle();
                if (!(conversationTitle == null && m == null)) {
                    if (conversationTitle != null) {
                        return conversationTitle;
                    }
                    return m.getSender();
                }
            }
            return super.resolveTitle();
        }

        @RestrictTo({Scope.GROUP_ID})
        protected BuilderExtender getExtender() {
            if (VERSION.SDK_INT >= 24) {
                return new Api24Extender();
            }
            if (VERSION.SDK_INT >= 21) {
                return new LollipopExtender();
            }
            if (VERSION.SDK_INT >= 16) {
                return new JellybeanExtender();
            }
            if (VERSION.SDK_INT >= 14) {
                return new IceCreamSandwichExtender();
            }
            return super.getExtender();
        }
    }

    /* renamed from: android.support.v7.app.NotificationCompat$DecoratedCustomViewStyle */
    public static class DecoratedCustomViewStyle extends Style {
    }

    /* renamed from: android.support.v7.app.NotificationCompat$MediaStyle */
    public static class MediaStyle extends Style {
        int[] mActionsToShowInCompact = null;
        PendingIntent mCancelButtonIntent;
        boolean mShowCancelButton;
        Token mToken;

        public MediaStyle(Builder builder) {
            setBuilder(builder);
        }

        public MediaStyle setShowActionsInCompactView(int... actions) {
            this.mActionsToShowInCompact = actions;
            return this;
        }

        public MediaStyle setMediaSession(Token token) {
            this.mToken = token;
            return this;
        }

        public MediaStyle setShowCancelButton(boolean show) {
            this.mShowCancelButton = show;
            return this;
        }

        public MediaStyle setCancelButtonIntent(PendingIntent pendingIntent) {
            this.mCancelButtonIntent = pendingIntent;
            return this;
        }
    }

    /* renamed from: android.support.v7.app.NotificationCompat$DecoratedMediaCustomViewStyle */
    public static class DecoratedMediaCustomViewStyle extends MediaStyle {
    }

    /* renamed from: android.support.v7.app.NotificationCompat$IceCreamSandwichExtender */
    private static class IceCreamSandwichExtender extends BuilderExtender {
        IceCreamSandwichExtender() {
        }

        public Notification build(Builder b, NotificationBuilderWithBuilderAccessor builder) {
            RemoteViews contentView = NotificationCompat.addStyleGetContentViewIcs(builder, b);
            Notification n = builder.build();
            if (contentView != null) {
                n.contentView = contentView;
            } else if (b.getContentView() != null) {
                n.contentView = b.getContentView();
            }
            return n;
        }
    }

    /* renamed from: android.support.v7.app.NotificationCompat$JellybeanExtender */
    private static class JellybeanExtender extends BuilderExtender {
        JellybeanExtender() {
        }

        public Notification build(Builder b, NotificationBuilderWithBuilderAccessor builder) {
            RemoteViews contentView = NotificationCompat.addStyleGetContentViewJellybean(builder, b);
            Notification n = builder.build();
            if (contentView != null) {
                n.contentView = contentView;
            }
            NotificationCompat.addBigStyleToBuilderJellybean(n, b);
            return n;
        }
    }

    /* renamed from: android.support.v7.app.NotificationCompat$LollipopExtender */
    private static class LollipopExtender extends BuilderExtender {
        LollipopExtender() {
        }

        public Notification build(Builder b, NotificationBuilderWithBuilderAccessor builder) {
            RemoteViews contentView = NotificationCompat.addStyleGetContentViewLollipop(builder, b);
            Notification n = builder.build();
            if (contentView != null) {
                n.contentView = contentView;
            }
            NotificationCompat.addBigStyleToBuilderLollipop(n, b);
            NotificationCompat.addHeadsUpToBuilderLollipop(n, b);
            return n;
        }
    }

    public static Token getMediaSession(Notification notification) {
        Bundle extras = android.support.p000v4.app.NotificationCompat.getExtras(notification);
        if (extras != null) {
            if (VERSION.SDK_INT >= 21) {
                Parcelable tokenInner = extras.getParcelable(android.support.p000v4.app.NotificationCompat.EXTRA_MEDIA_SESSION);
                if (tokenInner != null) {
                    return Token.fromToken(tokenInner);
                }
            }
            IBinder tokenInner2 = BundleCompat.getBinder(extras, android.support.p000v4.app.NotificationCompat.EXTRA_MEDIA_SESSION);
            if (tokenInner2 != null) {
                Parcel p = Parcel.obtain();
                p.writeStrongBinder(tokenInner2);
                p.setDataPosition(0);
                Token token = (Token) Token.CREATOR.createFromParcel(p);
                p.recycle();
                return token;
            }
        }
        return null;
    }

    private static void addStyleToBuilderApi24(NotificationBuilderWithBuilderAccessor builder, Builder b) {
        if (b.mStyle instanceof DecoratedCustomViewStyle) {
            NotificationCompatImpl24.addDecoratedCustomViewStyle(builder);
        } else if (b.mStyle instanceof DecoratedMediaCustomViewStyle) {
            NotificationCompatImpl24.addDecoratedMediaCustomViewStyle(builder);
        } else if (!(b.mStyle instanceof MessagingStyle)) {
            NotificationCompat.addStyleGetContentViewLollipop(builder, b);
        }
    }

    private static RemoteViews addStyleGetContentViewLollipop(NotificationBuilderWithBuilderAccessor builder, Builder b) {
        if (b.mStyle instanceof MediaStyle) {
            MediaStyle mediaStyle = (MediaStyle) b.mStyle;
            NotificationCompatImpl21.addMediaStyle(builder, mediaStyle.mActionsToShowInCompact, mediaStyle.mToken != null ? mediaStyle.mToken.getToken() : null);
            boolean hasContentView = b.getContentView() != null;
            boolean isMorL = VERSION.SDK_INT >= 21 && VERSION.SDK_INT <= 23;
            boolean createCustomContent = hasContentView || (isMorL && b.getBigContentView() != null);
            if (!(b.mStyle instanceof DecoratedMediaCustomViewStyle) || !createCustomContent) {
                return null;
            }
            RemoteViews contentViewMedia = NotificationCompatImplBase.overrideContentViewMedia(builder, b.mContext, b.mContentTitle, b.mContentText, b.mContentInfo, b.mNumber, b.mLargeIcon, b.mSubText, b.mUseChronometer, b.getWhenIfShowing(), b.getPriority(), b.mActions, mediaStyle.mActionsToShowInCompact, false, null, hasContentView);
            if (hasContentView) {
                NotificationCompatImplBase.buildIntoRemoteViews(b.mContext, contentViewMedia, b.getContentView());
            }
            NotificationCompat.setBackgroundColor(b.mContext, contentViewMedia, b.getColor());
            return contentViewMedia;
        } else if (b.mStyle instanceof DecoratedCustomViewStyle) {
            return NotificationCompat.getDecoratedContentView(b);
        } else {
            return NotificationCompat.addStyleGetContentViewJellybean(builder, b);
        }
    }

    private static RemoteViews addStyleGetContentViewJellybean(NotificationBuilderWithBuilderAccessor builder, Builder b) {
        if (b.mStyle instanceof MessagingStyle) {
            NotificationCompat.addMessagingFallBackStyle((MessagingStyle) b.mStyle, builder, b);
        }
        return NotificationCompat.addStyleGetContentViewIcs(builder, b);
    }

    private static Message findLatestIncomingMessage(MessagingStyle style) {
        List<Message> messages = style.getMessages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message m = (Message) messages.get(i);
            if (!TextUtils.isEmpty(m.getSender())) {
                return m;
            }
        }
        if (messages.isEmpty()) {
            return null;
        }
        return (Message) messages.get(messages.size() - 1);
    }

    private static CharSequence makeMessageLine(Builder b, MessagingStyle style, Message m) {
        BidiFormatter bidi = BidiFormatter.getInstance();
        SpannableStringBuilder sb = new SpannableStringBuilder();
        boolean afterLollipop = VERSION.SDK_INT >= 21;
        int color = (afterLollipop || VERSION.SDK_INT <= 10) ? ViewCompat.MEASURED_STATE_MASK : -1;
        CharSequence replyName = m.getSender();
        if (TextUtils.isEmpty(m.getSender())) {
            if (style.getUserDisplayName() == null) {
                replyName = "";
            } else {
                replyName = style.getUserDisplayName();
            }
            if (afterLollipop && b.getColor() != 0) {
                color = b.getColor();
            }
        }
        CharSequence senderText = bidi.unicodeWrap(replyName);
        sb.append(senderText);
        sb.setSpan(NotificationCompat.makeFontColorSpan(color), sb.length() - senderText.length(), sb.length(), 33);
        sb.append("  ").append(bidi.unicodeWrap(m.getText() == null ? "" : m.getText()));
        return sb;
    }

    private static TextAppearanceSpan makeFontColorSpan(int color) {
        return new TextAppearanceSpan(null, 0, 0, ColorStateList.valueOf(color), null);
    }

    private static void addMessagingFallBackStyle(MessagingStyle style, NotificationBuilderWithBuilderAccessor builder, Builder b) {
        boolean showNames;
        SpannableStringBuilder completeMessage = new SpannableStringBuilder();
        List<Message> messages = style.getMessages();
        if (style.getConversationTitle() != null || NotificationCompat.hasMessagesWithoutSender(style.getMessages())) {
            showNames = true;
        } else {
            showNames = false;
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message m = (Message) messages.get(i);
            CharSequence line = showNames ? NotificationCompat.makeMessageLine(b, style, m) : m.getText();
            if (i != messages.size() - 1) {
                completeMessage.insert(0, "\n");
            }
            completeMessage.insert(0, line);
        }
        NotificationCompatImplJellybean.addBigTextStyle(builder, completeMessage);
    }

    private static boolean hasMessagesWithoutSender(List<Message> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (((Message) messages.get(i)).getSender() == null) {
                return true;
            }
        }
        return false;
    }

    private static RemoteViews addStyleGetContentViewIcs(NotificationBuilderWithBuilderAccessor builder, Builder b) {
        if (b.mStyle instanceof MediaStyle) {
            MediaStyle mediaStyle = (MediaStyle) b.mStyle;
            boolean isDecorated = (b.mStyle instanceof DecoratedMediaCustomViewStyle) && b.getContentView() != null;
            RemoteViews contentViewMedia = NotificationCompatImplBase.overrideContentViewMedia(builder, b.mContext, b.mContentTitle, b.mContentText, b.mContentInfo, b.mNumber, b.mLargeIcon, b.mSubText, b.mUseChronometer, b.getWhenIfShowing(), b.getPriority(), b.mActions, mediaStyle.mActionsToShowInCompact, mediaStyle.mShowCancelButton, mediaStyle.mCancelButtonIntent, isDecorated);
            if (isDecorated) {
                NotificationCompatImplBase.buildIntoRemoteViews(b.mContext, contentViewMedia, b.getContentView());
                return contentViewMedia;
            }
        } else if (b.mStyle instanceof DecoratedCustomViewStyle) {
            return NotificationCompat.getDecoratedContentView(b);
        }
        return null;
    }

    private static void addBigStyleToBuilderJellybean(Notification n, Builder b) {
        if (b.mStyle instanceof MediaStyle) {
            RemoteViews innerView;
            MediaStyle mediaStyle = (MediaStyle) b.mStyle;
            if (b.getBigContentView() != null) {
                innerView = b.getBigContentView();
            } else {
                innerView = b.getContentView();
            }
            boolean isDecorated = (b.mStyle instanceof DecoratedMediaCustomViewStyle) && innerView != null;
            NotificationCompatImplBase.overrideMediaBigContentView(n, b.mContext, b.mContentTitle, b.mContentText, b.mContentInfo, b.mNumber, b.mLargeIcon, b.mSubText, b.mUseChronometer, b.getWhenIfShowing(), b.getPriority(), 0, b.mActions, mediaStyle.mShowCancelButton, mediaStyle.mCancelButtonIntent, isDecorated);
            if (isDecorated) {
                NotificationCompatImplBase.buildIntoRemoteViews(b.mContext, n.bigContentView, innerView);
            }
        } else if (b.mStyle instanceof DecoratedCustomViewStyle) {
            NotificationCompat.addDecoratedBigStyleToBuilder(n, b);
        }
    }

    private static RemoteViews getDecoratedContentView(Builder b) {
        if (b.getContentView() == null) {
            return null;
        }
        RemoteViews remoteViews = NotificationCompatImplBase.applyStandardTemplateWithActions(b.mContext, b.mContentTitle, b.mContentText, b.mContentInfo, b.mNumber, b.mNotification.icon, b.mLargeIcon, b.mSubText, b.mUseChronometer, b.getWhenIfShowing(), b.getPriority(), b.getColor(), C0082R.layout.notification_template_custom_big, false, null);
        NotificationCompatImplBase.buildIntoRemoteViews(b.mContext, remoteViews, b.getContentView());
        return remoteViews;
    }

    private static void addDecoratedBigStyleToBuilder(Notification n, Builder b) {
        RemoteViews bigContentView = b.getBigContentView();
        RemoteViews innerView = bigContentView != null ? bigContentView : b.getContentView();
        if (innerView != null) {
            RemoteViews remoteViews = NotificationCompatImplBase.applyStandardTemplateWithActions(b.mContext, b.mContentTitle, b.mContentText, b.mContentInfo, b.mNumber, n.icon, b.mLargeIcon, b.mSubText, b.mUseChronometer, b.getWhenIfShowing(), b.getPriority(), b.getColor(), C0082R.layout.notification_template_custom_big, false, b.mActions);
            NotificationCompatImplBase.buildIntoRemoteViews(b.mContext, remoteViews, innerView);
            n.bigContentView = remoteViews;
        }
    }

    private static void addDecoratedHeadsUpToBuilder(Notification n, Builder b) {
        RemoteViews headsUp = b.getHeadsUpContentView();
        RemoteViews innerView = headsUp != null ? headsUp : b.getContentView();
        if (headsUp != null) {
            RemoteViews remoteViews = NotificationCompatImplBase.applyStandardTemplateWithActions(b.mContext, b.mContentTitle, b.mContentText, b.mContentInfo, b.mNumber, n.icon, b.mLargeIcon, b.mSubText, b.mUseChronometer, b.getWhenIfShowing(), b.getPriority(), b.getColor(), C0082R.layout.notification_template_custom_big, false, b.mActions);
            NotificationCompatImplBase.buildIntoRemoteViews(b.mContext, remoteViews, innerView);
            n.headsUpContentView = remoteViews;
        }
    }

    private static void addBigStyleToBuilderLollipop(Notification n, Builder b) {
        RemoteViews innerView;
        if (b.getBigContentView() != null) {
            innerView = b.getBigContentView();
        } else {
            innerView = b.getContentView();
        }
        if ((b.mStyle instanceof DecoratedMediaCustomViewStyle) && innerView != null) {
            NotificationCompatImplBase.overrideMediaBigContentView(n, b.mContext, b.mContentTitle, b.mContentText, b.mContentInfo, b.mNumber, b.mLargeIcon, b.mSubText, b.mUseChronometer, b.getWhenIfShowing(), b.getPriority(), 0, b.mActions, false, null, true);
            NotificationCompatImplBase.buildIntoRemoteViews(b.mContext, n.bigContentView, innerView);
            NotificationCompat.setBackgroundColor(b.mContext, n.bigContentView, b.getColor());
        } else if (b.mStyle instanceof DecoratedCustomViewStyle) {
            NotificationCompat.addDecoratedBigStyleToBuilder(n, b);
        }
    }

    private static void setBackgroundColor(Context context, RemoteViews views, int color) {
        if (color == 0) {
            color = context.getResources().getColor(C0082R.color.notification_material_background_media_default_color);
        }
        views.setInt(C0082R.C0081id.status_bar_latest_event_content, "setBackgroundColor", color);
    }

    private static void addHeadsUpToBuilderLollipop(Notification n, Builder b) {
        RemoteViews innerView;
        if (b.getHeadsUpContentView() != null) {
            innerView = b.getHeadsUpContentView();
        } else {
            innerView = b.getContentView();
        }
        if ((b.mStyle instanceof DecoratedMediaCustomViewStyle) && innerView != null) {
            n.headsUpContentView = NotificationCompatImplBase.generateMediaBigView(b.mContext, b.mContentTitle, b.mContentText, b.mContentInfo, b.mNumber, b.mLargeIcon, b.mSubText, b.mUseChronometer, b.getWhenIfShowing(), b.getPriority(), 0, b.mActions, false, null, true);
            NotificationCompatImplBase.buildIntoRemoteViews(b.mContext, n.headsUpContentView, innerView);
            NotificationCompat.setBackgroundColor(b.mContext, n.headsUpContentView, b.getColor());
        } else if (b.mStyle instanceof DecoratedCustomViewStyle) {
            NotificationCompat.addDecoratedHeadsUpToBuilder(n, b);
        }
    }
}
