package com.example.systeminteraction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
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

    private static final int REQUEST_CALL_PHONE_PERMISSION = 1;
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 2;

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
        } else if (requestCode == REQUEST_CALL_PHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено, совершаем звонок
                makePhoneCall();
            } else {
                // Разрешение не предоставлено, выводим сообщение об ошибке
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено, совершаем звонок
                sendSMS();
            } else {
                // Разрешение не предоставлено, выводим сообщение об ошибке
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
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем разрешение на совершение звонка, если оно не предоставлено
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE_PERMISSION);
        } else {
            // Разрешение уже предоставлено, совершаем звонок
            startActivity(callIntent);
        }
    }

    public void sendSMS() {
        String message = editTextMessage.getText().toString();
        sendMessage(phoneNumber, message);
    }

    private void sendMessage(String phoneNumber, String message) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        } else {
            // Получаем экземпляр SmsManager
            SmsManager smsManager = SmsManager.getDefault();
            // Отправляем SMS
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            // Оповещаем пользователя об успешной отправке
            Toast.makeText(this, "SMS sent", Toast.LENGTH_SHORT).show();
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
