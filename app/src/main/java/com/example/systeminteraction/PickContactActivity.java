package com.example.systeminteraction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PickContactActivity extends AppCompatActivity {

    private String phoneNumber;
    EditText editTextMessage;
    private ActivityResultLauncher<Intent> pickContactLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_interaction);

        Button buttonCall = findViewById(R.id.button_call);
        Button buttonSendMessage = findViewById(R.id.button_send_message);
        editTextMessage = findViewById(R.id.editText_message);

        // Инициализируем лаунчер для выбора контакта
        pickContactLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            // Обработка выбранного контакта
                            Uri contactUri = data.getData();
                            handleSelectedContact(contactUri);
                        }
                    }
                });

        // Запрашивать разрешение на чтение контактов, если оно не предоставлено
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Запросить разрешение
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PackageManager.PERMISSION_GRANTED);
        } else {
            // Разрешение уже предоставлено, открытие средства выбора контактов
            openContactPicker();
        }

        buttonCall.setOnClickListener(v -> makePhoneCall());
        buttonSendMessage.setOnClickListener(v -> sendSMS());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено, открытие средства выбора контактов
                openContactPicker();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openContactPicker() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        pickContactLauncher.launch(pickContactIntent);
    }

    private void handleSelectedContact(Uri contactUri) {
        phoneNumber = getPhoneNumber(contactUri);
        String contactName = getContactName(contactUri);

        // Отображение имени и номера телефона в TextViews
        TextView textViewName = findViewById(R.id.textView_name);
        TextView textViewPhone = findViewById(R.id.textView_phone);
        textViewName.setText(String.format("Name: %s", contactName));
        textViewPhone.setText(String.format("Phone: %s", phoneNumber));
    }

    public void makePhoneCall() {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phoneNumber));
        if (dialIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(dialIntent);
        } else {
            Toast.makeText(this, "No app found to handle this action", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSMS() {
        String message = editTextMessage.getText().toString();
        sendMessage(phoneNumber, message);
    }

    private void sendMessage(String phoneNumber, String message) {
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
        smsIntent.putExtra("sms_body", message);
        if (smsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(smsIntent);
        } else {
            Toast.makeText(this, "No app found to handle this action", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("Range")
    private String getPhoneNumber(Uri contactUri) {
        String phoneNumber = null;
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phoneCursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{contactId},
                    null
            );
            if (phoneCursor != null && phoneCursor.moveToFirst()) {
                phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneCursor.close();
            }
            cursor.close();
        }
        return phoneNumber;
    }

    private String getContactName(Uri contactUri) {
        String contactName = null;
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            contactName = cursor.getString(displayNameIndex);
            cursor.close();
        }
        return contactName;
    }
}
