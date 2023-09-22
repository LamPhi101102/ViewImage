package com.example.mobilelogbookviewimage;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mobilelogbookviewimage.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<String> encodedImages = new ArrayList<>();
    private List<Bitmap> originalImages = new ArrayList<>();
    private int currentImageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.selectFromDrive.setOnClickListener(v -> {
            originalImages.clear();
            encodedImages.clear();
            // Create an intent to open the document picker with multiple selections
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImage.launch(intent);
        });
        binding.selectFromResource.setOnClickListener(v -> {
            loadImagesFromResources();
        });

        ImageView buttonNext = findViewById(R.id.buttonNext);
        ImageView buttonPrevious = findViewById(R.id.buttonPrevious);

        buttonNext.setOnClickListener(view -> {
            if (!encodedImages.isEmpty()) {
                currentImageIndex++;
                if (currentImageIndex >= encodedImages.size()) {
                    // If we're at the end, loop back to the first image
                    currentImageIndex = 0;
                }
                updateImageView();
            }else{
                ShowToast(this, "There are any images");

            }
        });

        buttonPrevious.setOnClickListener(view -> {
            if (!encodedImages.isEmpty()) {
                currentImageIndex--;
                if (currentImageIndex < 0) {
                    // If we're at the beginning, go to the last image
                    currentImageIndex = encodedImages.size() - 1;
                }
                updateImageView();
            }else{
                ShowToast(this, "There are any images");
            }
        });
        binding.clear.setOnClickListener(view -> {
            originalImages.clear();
            encodedImages.clear();
            binding.imageProfile.setImageResource(android.R.color.transparent);
            binding.textAddImage.setVisibility(View.VISIBLE);
            currentImageIndex = 0;
            ShowToast(this,"All Images are Deleted");
        });
    }

    private void loadImagesFromResources() {
        Resources resources = getResources();
        // Specify the resource IDs of the images you want to load from the drawable folder
        int resourceIndex = 1; // Start index for your resources
        while (true) {
            // Construct the resource name dynamically
            String resourceName = "image" + resourceIndex;

            // Get the resource ID
            int resourceId = resources.getIdentifier(resourceName, "drawable", getPackageName());

            // Check if the resource exists
            if (resourceId != 0) {
                // Resource found, you can use it
                Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId);
                originalImages.add(bitmap);
                String encodedImage = encodedImage(bitmap); // Replace this with your encoding logic
                encodedImages.add(encodedImage);

                // Increment the resource index for the next iteration
                resourceIndex++;
            } else {
                // Resource not found, exit the loop
                break;
            }
        }
        int indexImages = resourceIndex -1;
        ShowToast(this,"Added " + indexImages + " Images from resources");

        // Now, you have loaded and processed all images dynamically

        // Display the first image
        int currentImageIndex = 0;
        updateImageView();
    }

    private void ShowToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void updateImageView() {
        if (!encodedImages.isEmpty() && currentImageIndex < encodedImages.size()) {
            Bitmap bitmap = originalImages.get(currentImageIndex);

            // Display the selected image
            binding.imageProfile.setImageBitmap(bitmap);
            binding.textAddImage.setVisibility(View.GONE);
        } else {
            // No images to display
            binding.imageProfile.setImageResource(android.R.color.transparent);
            binding.textAddImage.setVisibility(View.VISIBLE);// Clear the ImageView
        }
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        // Handle multiple selected images
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                processSelectedImage(imageUri);
                            }
                        }
                        // Handle single selected image
                        else if (result.getData().getData() != null) {
                            Uri imageUri = result.getData().getData();
                            processSelectedImage(imageUri);
                        }
                        ShowToast(this, "Added "+ originalImages.size()+ " Images From Drive");
                    }
                }
            }
    );
    private void processSelectedImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            originalImages.add(bitmap);
            String encodedImage = encodedImage(bitmap);
            encodedImages.add(encodedImage);
            if (originalImages.size() == 1) {
                updateImageView();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String encodedImage(Bitmap bitmap){
        // Bitmap lớp thể hiện một bức ảnh
        // First we determine the size
        // we fixed width
        int previewWidth = 150;
        // Calculate the height to the image not changed too much (biến dạng nhiều)
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        // then we can review the image with these size but in small size then it is easy to store and transfer
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        //ByteArrayOutputStream, here I used it to store image after compressing
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // using preview image to compress with the quality 50 and finally it will store in byteArrayOutputStream
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        // then transfer to byte arrays
        byte[] bytes = byteArrayOutputStream.toByteArray();
        // use Base64 encrypt bytes array to string based64
        return Base64.encodeToString(bytes, Base64.DEFAULT);
        // the result we will be used to transfer or store under the string
    }
}