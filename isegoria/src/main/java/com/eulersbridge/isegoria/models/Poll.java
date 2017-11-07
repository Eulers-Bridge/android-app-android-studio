package com.eulersbridge.isegoria.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Seb on 04/11/2017.
 */

public class Poll implements Parcelable {

    private long id;

    private String creatorEmail;
    private User creator;

    private String question;
    private ArrayList<PollOption> options;

    public Poll(JSONObject jsonObject) {
        try {
            id = jsonObject.getInt("nodeId");
            creatorEmail = jsonObject.optString("creatorEmail");
            question = jsonObject.getString("question");

            JSONArray optionsJson = jsonObject.getJSONArray("pollOptions");

            options = new ArrayList<>();

            for (int j = 0; j < optionsJson.length(); j++) {
                JSONObject optionObject = optionsJson.getJSONObject(j);
                PollOption option = new PollOption(optionObject);
                options.add(option);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Poll(Parcel in) {
        id = in.readLong();
        creatorEmail = in.readString();
        question = in.readString();

        PollOption[] optionsArray = in.createTypedArray(PollOption.CREATOR);

        options = new ArrayList<>(Arrays.asList(optionsArray));
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(creatorEmail);
        parcel.writeString(question);


        PollOption[] optionsArray = new PollOption[options.size()];
        // Pass a type-hint to `toArray`
        optionsArray = options.toArray(optionsArray);

        parcel.writeTypedArray(optionsArray, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Poll> CREATOR = new Parcelable.Creator<Poll>() {
        @Override
        public Poll createFromParcel(Parcel in) {
            return new Poll(in);
        }

        @Override
        public Poll[] newArray(int size) {
            return new Poll[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public ArrayList<PollOption> getOptions() {
        return options;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}
