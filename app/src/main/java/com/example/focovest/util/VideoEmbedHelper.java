package com.example.focovest.util;

import android.net.Uri;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoEmbedHelper {

    public static class Embed {
        public final String provider;
        public final String src;

        public Embed(String provider, String src) {
            this.provider = provider;
            this.src = src;
        }
    }

    private static int parseStart(String t) {
        if (t == null || t.isEmpty()) return 0;
        if (t.matches("^\\d+$")) return Integer.parseInt(t);
        Matcher m = Pattern.compile("(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?").matcher(t);
        if (!m.matches()) return 0;
        int h = m.group(1) != null ? Integer.parseInt(m.group(1)) : 0;
        int min = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
        int s = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
        return h * 3600 + min * 60 + s;
    }

    public static Embed toEmbed(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            Uri u = Uri.parse(raw.trim());
            String host = u.getHost();
            if (host == null) return null;
            host = host.replaceFirst("^(www|m)\\.", "");
            String path = u.getPath() != null ? u.getPath() : "";

            String id = null;
            if ("youtu.be".equals(host)) {
                String[] parts = path.replaceFirst("^/", "").split("/");
                if (parts.length > 0) id = parts[0];
            } else if (host.endsWith("youtube.com") || "youtube-nocookie.com".equals(host)) {
                if ("/watch".equals(path)) {
                    id = u.getQueryParameter("v");
                }
                if (id == null) {
                    Matcher m = Pattern.compile("/(?:embed|shorts|live|v)/([\\w-]{6,})").matcher(path);
                    if (m.find()) id = m.group(1);
                }
            }

            if (id != null && id.matches("^[\\w-]{6,}$")) {
                String tParam = u.getQueryParameter("t");
                if (tParam == null) tParam = u.getQueryParameter("start");
                int start = parseStart(tParam);
                String src = "https://www.youtube-nocookie.com/embed/" + id +
                        "?rel=0&modestbranding=1" + (start > 0 ? "&start=" + start : "");
                return new Embed("YouTube", src);
            }

            if (host.endsWith("vimeo.com")) {
                Matcher m = Pattern.compile("(\\d{5,})").matcher(path);
                if (m.find()) {
                    return new Embed("Vimeo", "https://player.vimeo.com/video/" + m.group(1));
                }
            }

            if (host.endsWith("dailymotion.com") || "dai.ly".equals(host)) {
                String vid = null;
                if ("dai.ly".equals(host)) {
                    vid = path.replaceFirst("^/", "");
                } else {
                    Matcher m = Pattern.compile("video/([a-zA-Z0-9]+)").matcher(path);
                    if (m.find()) vid = m.group(1);
                }
                if (vid != null && !vid.isEmpty()) {
                    return new Embed("Dailymotion", "https://www.dailymotion.com/embed/video/" + vid);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
