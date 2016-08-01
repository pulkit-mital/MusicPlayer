
package com.pulkit.musicplayer.fragments;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pulkit.musicplayer.R;
import com.pulkit.musicplayer.activity.AlbumArtistDetails;
import com.pulkit.musicplayer.adapter.CursorRecyclerViewAdapter;
import com.pulkit.musicplayer.phonemedia.MusicPlayerUtility;
import com.pulkit.musicplayer.phonemedia.MusicIndexer;
import com.pulkit.musicplayer.phonemedia.MediaControllers;
import com.pulkit.musicplayer.utils.Logger;


/**
 * @author PULKIT MITAL
 * @date 31/7/2016
 */

public class GenresFragment extends Fragment {

    private static final String TAG = "ArtistsFragment";
    private static Context context;
    private RecyclerView recyclerView;
    boolean mIsUnknownArtist;
    boolean mIsUnknownAlbum;
    private GenersRecyclerAdapter mAdapter;

    public static GenresFragment newInstance(int position, Context mContext) {
        GenresFragment f = new GenresFragment();
        context = mContext;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child_album, null);
        setupView(view);
        return view;
    }

    private void setupView(View v) {
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        populateData();
    }

    private void populateData() {
        mAdapter = (GenersRecyclerAdapter) getActivity().getLastNonConfigurationInstance();
        if (mAdapter == null) {
            mAdapter = new GenersRecyclerAdapter(getActivity(), null);
            recyclerView.setAdapter(mAdapter);
            getGenersCursor(mAdapter.getQueryHandler(), null);
        } else {
            recyclerView.setAdapter(mAdapter);
            mArtistCursor = mAdapter.getCursor();
            if (mArtistCursor != null) {
                init(mArtistCursor);
            } else {
                getGenersCursor(mAdapter.getQueryHandler(), null);
            }
        }
    }

    public void resetView() {
        recyclerView.scrollToPosition(0);
    }

    private Cursor mArtistCursor;

    public void init(Cursor c) {
        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(c); // also sets mArtistCursor
        if (mArtistCursor == null) {
            MusicPlayerUtility.displayDatabaseError(getActivity());
            mReScanHandler.sendEmptyMessageDelayed(0, 1000);
            return;
        }
    }

    private Handler mReScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mAdapter != null) {
                getGenersCursor(mAdapter.getQueryHandler(), null);
            }
        }
    };

    private Cursor getGenersCursor(AsyncQueryHandler async, String filter) {

        String[] cols = new String[]{MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME};

        Uri uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
        if (!TextUtils.isEmpty(filter)) {
            uri = uri.buildUpon().appendQueryParameter("filter", Uri.encode(filter)).build();
        }

        Cursor ret = null;
        if (async != null) {
            async.startQuery(0, null, uri, cols, null, null, null);
        } else {
            ret = MusicPlayerUtility.query(getActivity(), uri, cols, null, null, null);
        }
        return ret;
    }

    public class GenersRecyclerAdapter extends CursorRecyclerViewAdapter<GenersRecyclerAdapter.ViewHolder> {

        private final BitmapDrawable mDefaultAlbumIcon;
        private int genenrsIdIdx;
        private int genersIdx;

        private final Context mContext;
        private final Resources mResources;
        private final String mUnknownArtist;
        private final StringBuilder mBuffer = new StringBuilder();
        private MusicIndexer mIndexer;
        private AsyncQueryHandler mQueryHandler;
        private String mConstraint = null;
        private boolean mConstraintIsValid = false;

        protected GenersRecyclerAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            mQueryHandler = new QueryHandler(context.getContentResolver());

            Resources r = context.getResources();
            mDefaultAlbumIcon = (BitmapDrawable) r.getDrawable(R.drawable.bg_default_album_art);
            // no filter or dither, it's a lot faster and we can't tell the
            // difference
            mDefaultAlbumIcon.setFilterBitmap(false);
            mDefaultAlbumIcon.setDither(false);

            mContext = context;
            getColumnIndices(cursor);
            mResources = context.getResources();
            mUnknownArtist = context.getString(R.string.unknown_artist_name);
        }

        class QueryHandler extends AsyncQueryHandler {
            QueryHandler(ContentResolver res) {
                super(res);
            }

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                init(cursor);
            }
        }

        public AsyncQueryHandler getQueryHandler() {
            return mQueryHandler;
        }

        @Override
        public void changeCursor(Cursor cursor) {
            if (getActivity().isFinishing() && cursor != null) {
                cursor.close();
                cursor = null;
            }
            if (cursor != mArtistCursor) {
                mArtistCursor = cursor;
                getColumnIndices(cursor);
                super.changeCursor(cursor);
            }
        }

        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            String s = constraint.toString();
            if (mConstraintIsValid && ((s == null && mConstraint == null) || (s != null && s.equals(mConstraint)))) {
                return getCursor();
            }
            Cursor c = getGenersCursor(null, s);
            mConstraint = s;
            mConstraintIsValid = true;
            return c;
        }

        private void getColumnIndices(Cursor cursor) {
            if (cursor != null) {
                genenrsIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID);
                genersIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);
                if (mIndexer != null) {
                    mIndexer.setCursor(cursor);
                } else {
                    mIndexer = new MusicIndexer(cursor, genersIdx, mResources.getString(R.string.fast_scroll_alphabet));
                }
            }
        }


        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {

            String displaygeners = cursor.getString(genersIdx);
            int genenrsId = cursor.getInt(genenrsIdIdx);

            boolean unknown = displaygeners == null || displaygeners.equals(MediaStore.UNKNOWN_STRING);
            if (unknown) {
                displaygeners = mUnknownArtist;
            }
            viewHolder.line1.setText(displaygeners);
            viewHolder.line2.setVisibility(View.GONE);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewgroup, int position) {
            return new ViewHolder(LayoutInflater.from(viewgroup.getContext()).inflate(R.layout.inflate_grid_item, viewgroup, false));
        }

        private long getGenerID(int position) {
            return getItemId(position);
        }


        protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView line1;
            TextView line2;
            ImageView icon;

            public ViewHolder(View itemView) {
                super(itemView);
                line1 = (TextView) itemView.findViewById(R.id.line1);
                line2 = (TextView) itemView.findViewById(R.id.line2);
                icon = (ImageView) itemView.findViewById(R.id.icon);
                icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                try {
                    long generid = getGenerID(getPosition());
                    Intent mIntent = new Intent(context, AlbumArtistDetails.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putLong("id", generid);
                    mBundle.putLong("tagfor", MediaControllers.SonLoadFor.Gener.ordinal());
                    mBundle.putString("albumname", ((TextView) view.findViewById(R.id.line1)).getText().toString().trim());
                    mBundle.putString("title_one", "All my songs");
                    mBundle.putString("title_sec", ((TextView) view.findViewById(R.id.line2)).getText().toString().trim());
                    mIntent.putExtras(mBundle);
                    ((Activity) context).startActivity(mIntent);
                    ((Activity) context).overridePendingTransition(0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.info(TAG, e.toString());
                }
            }
        }
    }
}
