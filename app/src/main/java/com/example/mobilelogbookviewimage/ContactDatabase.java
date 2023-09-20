package com.example.mobilelogbookviewimage;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.mobilelogbookviewimage.databinding.ActivityContactDatabaseBinding;
import com.example.mobilelogbookviewimage.databinding.ActivityMainBinding;

public class ContactDatabase extends AppCompatActivity {
    private ActivityContactDatabaseBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactDatabaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}