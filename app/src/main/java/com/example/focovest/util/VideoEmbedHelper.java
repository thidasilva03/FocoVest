package com.example.focovest.util;

import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoEmbedHelper {

    public static class Embed {
        public final String provider;
        public final String videoId;
        public final String src;

        public Embed(String provider, String videoId, String src) {
            this.provider = provider;
            this.videoId = videoId;
            this.src = src;
        }
    }

    private static int parseStart(String t) {
        if (t == null || t.isEmpty()) return 0;
        try {
            if (t.matches("^\\d+$")) {
                long v = Long.parseLong(t);
                return (int) Math.min(v, 86400L);
            }
            Matcher m = Pattern.compile("(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?").matcher(t);
            if (!m.matches()) return 0;
            long h = m.group(1) != null ? Long.parseLong(m.group(1)) : 0;
            long min = m.group(2) != null ? Long.parseLong(m.group(2)) : 0;
            long s = m.group(3) != null ? Long.parseLong(m.group(3)) : 0;
            return (int) Math.min(h * 3600 + min * 60 + s, 86400L);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static Embed toEmbed(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            Uri u = Uri.parse(raw.trim());
            String host = u.getHost();
            if (host == null) return null;
            host = host.toLowerCase(java.util.Locale.ROOT).replaceFirst("^(www|m)\\.", "");
            String path = u.getPath() != null ? u.getPath() : "";

            String id = null;
            if ("youtu.be".equals(host)) {
                String[] parts = path.replaceFirst("^/", "").split("/");
                if (parts.length > 0) id = parts[0];
            } else if ("youtube.com".equals(host) || host.endsWith(".youtube.com")
                    || "youtube-nocookie.com".equals(host)) {
                if ("/watch".equals(path)) {
                    id = u.getQueryParameter("v");
                }
                if (id == null) {
                    Matcher m = Pattern.compile("/(?:embed|shorts|live|v)/([\\w-]{6,20})").matcher(path);
                    if (m.find()) id = m.group(1);
                }
            }

            if (id != null && id.matches("^[\\w-]{6,20}$")) {
                String tParam = u.getQueryParameter("t");
                if (tParam == null) tParam = u.getQueryParameter("start");
                int start = parseStart(tParam);
                String src = "https://www.youtube-nocookie.com/embed/" + id
                        + "?rel=0&modestbranding=1&playsinline=1"
                        + (start > 0 ? "&start=" + start : "");
                return new Embed("YouTube", id, src);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gera HTML responsivo com iframe seguro para reprodução dentro do WebView.
     */
    public static String buildIframeHtml(Embed embed) {
        if (embed == null) return "";
        return "<!DOCTYPE html><html><head>"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1.0\"/>"
                + "<style>html,body{margin:0;padding:0;width:100%;height:100%;background:#16181D;overflow:hidden;}"
                + "iframe{width:100%;height:100%;border:0;}</style></head><body>"
                + "<iframe src=\"" + embed.src + "\" "
                + "allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" "
                + "allowfullscreen></iframe></body></html>";
    }
}
