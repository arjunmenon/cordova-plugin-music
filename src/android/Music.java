package com.cordova.music;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.provider.MediaStore;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Music  extends CordovaPlugin implements OnCompletionListener, OnPreparedListener {

    MediaPlayer player= new MediaPlayer();
    private CallbackContext messageChannel;
    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getPlaylists")) {
            ContentResolver contentResolver =this.cordova.getActivity().getContentResolver();
            String[] proj = {"*"};
            Uri tempPlaylistURI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
            final String idKey = MediaStore.Audio.Playlists._ID;
            final String nameKey = MediaStore.Audio.Playlists.NAME;

            Cursor playListCursor= contentResolver.query(tempPlaylistURI, proj, null, null, null);

            if(playListCursor == null){
                return false;
            }

            String playListName = null;
            String playListId = null;
            JSONArray res = new JSONArray();

            for(int i = 0; i <playListCursor.getCount() ; i++)
            {
                playListCursor.moveToPosition(i);
                playListId = playListCursor.getString(playListCursor.getColumnIndex(idKey));
                playListName = playListCursor.getString(playListCursor.getColumnIndex(nameKey));
                JSONObject r = new JSONObject();
                r.put("id",playListId);
                r.put("name",playListName);
                res.put(i,r);
            }
            if(playListCursor != null)
                playListCursor.close();
            callbackContext.success(res);
            return true;
        }
        else if (action.equals("getSongsFromPlaylist")) {
            ContentResolver contentResolver =this.cordova.getActivity().getContentResolver();
            Long playListID = args.getLong(0);
            String[] proj = {"*"};
            Uri psUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListID);
            

            Cursor psCursor = contentResolver.query(psUri, proj, null, null, null);

            if(psCursor == null){
                return false;
            }

            JSONArray psRes = new JSONArray();

            for(int i = 0; i < psCursor.getCount() ; i++)
            {
                psCursor.moveToPosition(i);
                JSONObject r = new JSONObject();
                r.put("id", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID))));
                r.put("name", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media.TITLE))));
                r.put("artist", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))));
                psRes.put(i,r);
            }
            if(psCursor != null)
                psCursor.close();
            callbackContext.success(psRes);
            return true;
        }
        else if (action.equals("getSongs")) {
            ContentResolver contentResolver =this.cordova.getActivity().getContentResolver();
            String[] proj = {"*"};
            Uri psUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            Cursor psCursor = contentResolver.query(psUri, proj, selection, null, null);

            if(psCursor == null){
                return false;
            }
            
            private final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

            JSONArray psRes = new JSONArray();

            for(int i = 0; i < psCursor.getCount() ; i++)
            {
                psCursor.moveToPosition(i);
                JSONObject r = new JSONObject();
                int albumId = psCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                r.put("id", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media._ID))));
                r.put("name", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media.TITLE))));
                r.put("artist", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))));
                //System.out.println(psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media.DATA))));
                r.put("path", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media.DATA))));
                r.put("album", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))));
                r.put("album_id", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))));
                //r.put("album_key", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY))));
                r.put("duration", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));
                r.put("is_music", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC))));
                r.put("cover_art", ContentUris.withAppendedId(albumArtUri, psCursor.getLong(albumId)));
                psRes.put(i,r);
            }
            if(psCursor != null)
                psCursor.close();
            callbackContext.success(psRes);
            return true;
        }
        else if (action.equals("getAlbums")) {
            ContentResolver contentResolver =this.cordova.getActivity().getContentResolver();
            //String[] proj = new String[] { Albums._ID, Albums.ALBUM, Albums.ARTIST, Albums.ALBUM_ART, Albums.NUMBER_OF_SONGS };
            String[] proj = {"*"};
            Uri psUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            
            String selection = null;
            String[] selectionArgs = null;
            //String sortOrder = Media.ALBUM + " ASC";
            String sortOrder = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";

            Cursor psCursor = contentResolver.query(psUri, proj, selection, selectionArgs, sortOrder);

            if(psCursor == null){
                return false;
            }

            JSONArray psRes = new JSONArray();

            for(int i = 0; i < psCursor.getCount() ; i++)
            {
                psCursor.moveToPosition(i);
                JSONObject r = new JSONObject();
                r.put("album_id", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Albums._ID))));
                r.put("album_name", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM))));
                r.put("album_artist", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST))));
                r.put("album_art", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))));
                r.put("album_total_tracks", psCursor.getString((psCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS))));
                psRes.put(i,r);
            }
            if(psCursor != null)
                psCursor.close();
            callbackContext.success(psRes);
            return true;
        }
        else if (action.equals("playSong")) {
            ContentResolver contentResolver =this.cordova.getActivity().getContentResolver();
            String songID = args.getString(0);
            String[] proj = {"*"};
            Uri psUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            final String sIdKey = MediaStore.Audio.Media._ID;
            final String sDataKey = MediaStore.Audio.Media.DATA;
            final String sTitleKey = MediaStore.Audio.Media.TITLE;
            final String selection=  MediaStore.Audio.Media._ID + " == "+songID;;

            Cursor sCurosr = contentResolver.query(psUri, proj, selection, null, null);

            if(sCurosr == null){
                return false;
            }
            sCurosr.moveToPosition(0);
            final Uri sUri= Uri.parse(sCurosr.getString(sCurosr.getColumnIndex(sDataKey)));
            if (sUri == null) {
                callbackContext.error("Called playAudio with null data stream.");
                return false;
            }
            try {
                if(player.isPlaying()){
                    player.seekTo(0);
                    player.pause();
                }
                this.player.reset();
                this.player.setDataSource(this.cordova.getActivity().getApplicationContext(), sUri);
                this.player.prepare();
                this.player.setOnPreparedListener(this);
                player.start();
            } catch (Exception e) {
                callbackContext.error("Failed to start MediaPlayer: " + e.getMessage());
                return false;
            }
            if(sCurosr!= null)
                sCurosr.close();
            return true;
        }
        else if (action.equals("stopSong")) {
            try {
                if(player.isPlaying()){
                    player.pause();
                    player.seekTo(0);
                    callbackContext.success("song stopped");
                }
            } catch (Exception e) {
                callbackContext.error("Failed to stop MediaPlayer: " + e.getMessage());
                return false;
            }
            return true;
        }
        else if (action.equals("messageChannel")) {
            messageChannel = callbackContext;
            return true;
        }
        else {
            return false;
        }
    }
    public void onCompletion(MediaPlayer player) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "Audio Completed");
        pluginResult.setKeepCallback(true);
        if (messageChannel != null) {
            messageChannel.sendPluginResult(pluginResult);
        }
    }

    public void onPrepared(MediaPlayer player) {
        // Listener for playback completion
        this.player.setOnCompletionListener(this);
    }

}
