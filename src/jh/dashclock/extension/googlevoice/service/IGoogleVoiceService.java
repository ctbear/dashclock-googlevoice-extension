package jh.dashclock.extension.googlevoice.service;

import com.google.android.apps.dashclock.api.DashClockExtension;

import java.util.List;

/**
 * Interface for services that monitor Google Voice notifications
 */
public interface IGoogleVoiceService {
    public int getUnreadCount(DashClockExtension mContext);
    public String getSender(DashClockExtension mContext);
    public String getBody(DashClockExtension mContext);
    public List<String> getAllMessages(DashClockExtension mContext);
    public void destroy();
}
