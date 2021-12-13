/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.telephony.common;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Contact phone number and its meta data.
 */
public class PhoneNumber implements Parcelable {

    private final String mRawNumber;
    private final String mNormalizedNumber;

    @NonNull
    private final String mAccountName;
    @NonNull
    private final String mAccountType;

    private int mType;
    @Nullable
    private String mLabel;
    private boolean mIsPrimary;
    private long mId;
    private int mDataVersion;

    /** The favorite bit is from local database, presenting a
     *  {@link com.android.car.dialer.storage.FavoriteNumberEntity}. */
    private boolean mIsFavorite;

    static PhoneNumber fromCursor(Cursor cursor) {
        int typeColumn = cursor.getColumnIndex(Phone.TYPE);
        int labelColumn = cursor.getColumnIndex(Phone.LABEL);
        int numberColumn = cursor.getColumnIndex(Phone.NUMBER);
        int normalizedNumberColumn = cursor.getColumnIndex(Phone.NORMALIZED_NUMBER);
        int rawDataIdColumn = cursor.getColumnIndex(Phone._ID);
        int dataVersionColumn = cursor.getColumnIndex(Phone.DATA_VERSION);
        // IS_PRIMARY means primary entry of the raw contact and IS_SUPER_PRIMARY means primary
        // entry of the aggregated contact. It is guaranteed that only one data entry is super
        // primary.
        int isPrimaryColumn = cursor.getColumnIndex(Phone.IS_SUPER_PRIMARY);
        int accountNameColumn = cursor.getColumnIndex(RawContacts.ACCOUNT_NAME);
        int accountTypeColumn = cursor.getColumnIndex(RawContacts.ACCOUNT_TYPE);
        return PhoneNumber.newInstance(
                cursor.getString(numberColumn),
                cursor.getString(normalizedNumberColumn),
                cursor.getInt(typeColumn),
                cursor.getString(labelColumn),
                cursor.getInt(isPrimaryColumn) > 0,
                cursor.getLong(rawDataIdColumn),
                cursor.getString(accountNameColumn),
                cursor.getString(accountTypeColumn),
                cursor.getInt(dataVersionColumn));
    }

    /**
     * Creates a new {@link PhoneNumber}.
     *
     * @param rawNumber        A potential phone number.
     * @param normalizedNumber Normalized phone number.
     * @param type             The phone number type. See more at {@link Phone#TYPE}
     * @param label            The user defined label. See more at {@link Phone#LABEL}
     * @param isPrimary        Whether this is the primary entry of the aggregated contact it
     *                         belongs to. See more at {@link Phone#IS_SUPER_PRIMARY}.
     * @param id               The unique key for raw contact entry containing the phone number
     *                         entity. See more at {@link Phone#_ID}
     * @param dataVersion      The dataVersion of the raw contact entry record. See more at {@link
     *                         Phone#DATA_VERSION}
     */
    public static PhoneNumber newInstance(String rawNumber, String normalizedNumber, int type,
            @Nullable String label, boolean isPrimary, long id, String accountName,
            String accountType, int dataVersion) {
        return new PhoneNumber(rawNumber, normalizedNumber, type, label, isPrimary, id, accountName,
                accountType, dataVersion);
    }

    private PhoneNumber(String rawNumber, String normalizedNumber, int type, @Nullable String label,
            boolean isPrimary, long id, String accountName, String accountType, int dataVersion) {
        mRawNumber = rawNumber;
        mNormalizedNumber = TextUtils.isEmpty(normalizedNumber) ? rawNumber : normalizedNumber;
        mType = type;
        mLabel = label;
        mIsPrimary = isPrimary;
        mId = id;
        mAccountName = accountName == null ? "" : accountName;
        mAccountType = accountType == null ? "" : accountType;
        mDataVersion = dataVersion;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PhoneNumber
                && TextUtils.equals(mNormalizedNumber, ((PhoneNumber) obj).mNormalizedNumber)
                && mAccountName.equals(((PhoneNumber) obj).mAccountName)
                && mAccountType.equals(((PhoneNumber) obj).mAccountType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mNormalizedNumber, mAccountName, mAccountType);
    }

    /**
     * Returns if the phone number is the primary entry for the aggregated contact it belongs to.
     * See more at {@link Phone#IS_SUPER_PRIMARY}.
     */
    public boolean isPrimary() {
        return mIsPrimary;
    }

    /**
     * Returns a human readable string label. For example, Home, Work, etc.
     */
    public CharSequence getReadableLabel(Resources res) {
        return Phone.getTypeLabel(res, mType, mLabel);
    }

    /**
     * Gets a phone number in the international format if valid. Otherwise, returns the raw number.
     */
    public String getNormalizedNumber() {
        return mNormalizedNumber;
    }

    /**
     * Returns the raw number, the number that is input by the user
     */
    public String getRawNumber() {
        return mRawNumber;
    }

    /**
     * Gets the type of phone number, for example Home or Work. Possible values are defined in
     * {@link Phone}.
     */
    public int getType() {
        return mType;
    }

    public long getId() {
        return mId;
    }

    @Nullable
    public String getAccountName() {
        return mAccountName;
    }

    @Nullable
    public String getAccountType() {
        return mAccountType;
    }

    /**
     * Updates the favorite bit, which is local database. See
     * {@link com.android.car.dialer.storage.FavoriteNumberDatabase}.
     */
    public void setIsFavorite(boolean isFavorite) {
        mIsFavorite = isFavorite;
    }

    /** Returns if the phone number is favorite entry. */
    public boolean isFavorite() {
        return mIsFavorite;
    }

    /**
     * Each contact may have a few sources with the same phone number. Merge same phone numbers as
     * one.
     *
     * <p>As long as one of those phone numbers is primary entry of the aggregated contact, mark
     * the merged phone number as primary.
     */
    public PhoneNumber merge(PhoneNumber phoneNumber) {
        if (equals(phoneNumber)) {
            if (mDataVersion < phoneNumber.mDataVersion) {
                mDataVersion = phoneNumber.mDataVersion;
                mId = phoneNumber.mId;
                mIsPrimary |= phoneNumber.mIsPrimary;
                mType = phoneNumber.mType;
                mLabel = phoneNumber.mLabel;
            }
        }
        return this;
    }

    /**
     * Gets the user defined label for the the contact method.
     */
    @Nullable
    public String getLabel() {
        return mLabel;
    }

    @Override
    public String toString() {
        return getNormalizedNumber() + " " + mAccountName + " " + mAccountType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mType);
        dest.writeString(mLabel);
        dest.writeString(mRawNumber);
        dest.writeString(mNormalizedNumber);
        dest.writeBoolean(mIsPrimary);
        dest.writeLong(mId);
        dest.writeString(mAccountName);
        dest.writeString(mAccountType);
        dest.writeInt(mDataVersion);
        dest.writeBoolean(mIsFavorite);
    }

    public static Creator<PhoneNumber> CREATOR = new Creator<PhoneNumber>() {
        @Override
        public PhoneNumber createFromParcel(Parcel source) {
            int type = source.readInt();
            String label = source.readString();
            String rawNumber = source.readString();
            String normalizedNumber = source.readString();
            boolean isPrimary = source.readBoolean();
            long id = source.readLong();
            String accountName = source.readString();
            String accountType = source.readString();
            int dataVersion = source.readInt();
            PhoneNumber phoneNumber = new PhoneNumber(rawNumber, normalizedNumber, type, label,
                    isPrimary, id, accountName, accountType, dataVersion);
            phoneNumber.setIsFavorite(source.readBoolean());
            return phoneNumber;
        }

        @Override
        public PhoneNumber[] newArray(int size) {
            return new PhoneNumber[size];
        }
    };
}
