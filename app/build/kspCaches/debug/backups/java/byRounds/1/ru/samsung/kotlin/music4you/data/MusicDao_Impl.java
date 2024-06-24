package ru.samsung.kotlin.music4you.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MusicDao_Impl implements MusicDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Music> __insertionAdapterOfMusic;

  public MusicDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMusic = new EntityInsertionAdapter<Music>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `music` (`id`,`title`,`author`,`audioFilePath`,`duration`,`albumArtPath`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Music entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getAuthor());
        statement.bindString(4, entity.getAudioFilePath());
        statement.bindLong(5, entity.getDuration());
        if (entity.getAlbumArtPath() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getAlbumArtPath());
        }
      }
    };
  }

  @Override
  public Object insertMusic(final List<Music> music, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMusic.insert(music);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllMusic(final Continuation<? super List<Music>> $completion) {
    final String _sql = "SELECT * FROM music";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Music>>() {
      @Override
      @NonNull
      public List<Music> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAudioFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioFilePath");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfAlbumArtPath = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtPath");
          final List<Music> _result = new ArrayList<Music>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Music _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpAudioFilePath;
            _tmpAudioFilePath = _cursor.getString(_cursorIndexOfAudioFilePath);
            final int _tmpDuration;
            _tmpDuration = _cursor.getInt(_cursorIndexOfDuration);
            final String _tmpAlbumArtPath;
            if (_cursor.isNull(_cursorIndexOfAlbumArtPath)) {
              _tmpAlbumArtPath = null;
            } else {
              _tmpAlbumArtPath = _cursor.getString(_cursorIndexOfAlbumArtPath);
            }
            _item = new Music(_tmpId,_tmpTitle,_tmpAuthor,_tmpAudioFilePath,_tmpDuration,_tmpAlbumArtPath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object searchMusic(final String query,
      final Continuation<? super List<Music>> $completion) {
    final String _sql = "SELECT * FROM music WHERE title LIKE ? OR author LIKE ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Music>>() {
      @Override
      @NonNull
      public List<Music> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAudioFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioFilePath");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfAlbumArtPath = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtPath");
          final List<Music> _result = new ArrayList<Music>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Music _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpAudioFilePath;
            _tmpAudioFilePath = _cursor.getString(_cursorIndexOfAudioFilePath);
            final int _tmpDuration;
            _tmpDuration = _cursor.getInt(_cursorIndexOfDuration);
            final String _tmpAlbumArtPath;
            if (_cursor.isNull(_cursorIndexOfAlbumArtPath)) {
              _tmpAlbumArtPath = null;
            } else {
              _tmpAlbumArtPath = _cursor.getString(_cursorIndexOfAlbumArtPath);
            }
            _item = new Music(_tmpId,_tmpTitle,_tmpAuthor,_tmpAudioFilePath,_tmpDuration,_tmpAlbumArtPath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
