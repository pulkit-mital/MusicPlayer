

package com.pulkit.musicplayer.phonemedia;

import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.AlphabetIndexer;

/**
 * @author PULKIT MITAL
 * @date 31/7/2016
 */
public class MusicIndexer extends AlphabetIndexer {

	public MusicIndexer(Cursor cursor, int sortedColumnIndex, CharSequence alphabet) {
		super(cursor, sortedColumnIndex, alphabet);
	}

	@Override
	protected int compare(String word, String letter) {
		String wordKey = MediaStore.Audio.keyFor(word);
		String letterKey = MediaStore.Audio.keyFor(letter);
		if (wordKey.startsWith(letter)) {
			return 0;
		} else {
			return wordKey.compareTo(letterKey);
		}
	}
}
