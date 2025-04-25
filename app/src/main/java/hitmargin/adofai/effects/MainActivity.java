package hitmargin.adofai.effects;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.util.SparseBooleanArray;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText editTextFilePath;
    private Button buttonBackup;
    private Button buttonDeleteAllEffects;
    private Button buttonDeleteMostEffects;
    private Button buttonDeleteVisualEffects;
    private Button buttonDeleteMajorFilters;
    private Button buttonDeleteDecorations;
    private Button buttonCustomDelete;
    private EditText editTextCustomEffects;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ListView listViewEffects;
    private ArrayList<String> effectsList;
    private SparseBooleanArray selectedItems;
    private EffectsAdapter effectsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(
                getApplication(),
                "更新日期：2025.4.25\n作者：HitMargin | QQ：2228293026",
                Toast.LENGTH_SHORT);

        verifyStoragePermissions();

        editTextFilePath = findViewById(R.id.editTextFilePath);
        // buttonBackup = findViewById(R.id.buttonBackup);
        buttonDeleteAllEffects = findViewById(R.id.buttonDeleteAllEffects);
        buttonDeleteMostEffects = findViewById(R.id.buttonDeleteMostEffects);
        buttonDeleteVisualEffects = findViewById(R.id.buttonDeleteVisualEffects);
        buttonDeleteMajorFilters = findViewById(R.id.buttonDeleteMajorFilters);
        buttonDeleteDecorations = findViewById(R.id.buttonDeleteDecorations);
        buttonCustomDelete = findViewById(R.id.buttonCustomDelete);
        // editTextCustomEffects = findViewById(R.id.editTextCustomEffects);
        listViewEffects = findViewById(R.id.listViewEffects);
        effectsList =
                new ArrayList<>(
                        Arrays.asList(
                                "AnimateTrack",
                                "MoveTrack",
                                "MoveDecoration",
                                "Checkpoint",
                                "SetText",
                                "PositionTrack",
                                "RecolorTrack",
                                "ColorTrack",
                                "CustomBackground",
                                "Flash",
                                "MoveCamera",
                                "SetFilter",
                                "HallOfMirrors",
                                "ShakeScreen",
                                "Bloom",
                                "ScreenTile",
                                "ScreenScroll",
                                "RepeatEvents",
                                "ConditionalEvents",
                                "AddDecoration",
                                "AddText",
                                "SetFrameRate",
                                "SetFloorIcon",
                                "SetObject",
                                "AddObject",
                                "SetFilterAdvanced",
                                "SetDefaultText",
                                "EmitParticle",
                                "SetParticle",
                                "AddParticle",
                                "SetInputEvent",
                                "SetHoldSound",
                                "FreeRoam",
                                "FreeRoamTwirl",
                                "FreeRoamRemove",
                                "Hide",
                                "ScaleMargin",
                                "ScaleRadius"));
        effectsAdapter = new EffectsAdapter(this, effectsList);
        listViewEffects.setAdapter(effectsAdapter);
        // listViewEffects.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        selectedItems = new SparseBooleanArray(); // 初始化选中状态跟踪数组

        setupButtonListeners();
        listViewEffects.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        // 切换选中状态
                        effectsAdapter.toggleSelection(position);
                    }
                });
    }

    private void verifyStoragePermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11及以上，需要MANAGE_EXTERNAL_STORAGE权限
            if (!Environment.isExternalStorageManager()) {
                new AlertDialog.Builder(this)
                        .setTitle("需要文件访问权限")
                        .setMessage("请允许访问所有文件以继续")
                        .setPositiveButton(
                                "去设置",
                                (dialog, which) -> {
                                    try {
                                        Intent intent =
                                                new Intent(
                                                        Settings
                                                                .ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                        intent.setData(
                                                Uri.fromParts("package", getPackageName(), null));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        Intent intent = new Intent();
                                        intent.setAction(
                                                Settings
                                                        .ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                        startActivity(intent);
                                    }
                                })
                        .setNegativeButton("取消", null)
                        .show();
            }
        } else {
            // Android 10及以下版本，请求读写权限
            int readPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            ArrayList<String> permissionsNeeded = new ArrayList<>();
            if (readPermission != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissionsNeeded.isEmpty()) {
                requestPermissions(
                        permissionsNeeded.toArray(new String[0]), REQUEST_EXTERNAL_STORAGE);
            }
        }
    }

    private ArrayList<String> getEffectsList(int choice) {
        ArrayList<String> effectsList = new ArrayList<>();
        if (choice == 1) {
            effectsList.addAll(
                    Arrays.asList(
                            "AnimateTrack",
                            "MoveTrack",
                            "MoveDecoration",
                            "SetText",
                            "PositionTrack",
                            "RecolorTrack",
                            "ColorTrack",
                            "CustomBackground",
                            "Flash",
                            "MoveCamera",
                            "SetFilter",
                            "HallOfMirrors",
                            "ShakeScreen",
                            "Bloom",
                            "ScreenTile",
                            "ScreenScroll",
                            "RepeatEvents",
                            "ConditionalEvents",
                            "AddDecoration",
                            "AddText",
                            "SetFrameRate",
                            "SetFloorIcon",
                            "SetObject",
                            "AddObject",
                            "SetFilterAdvanced",
                            "SetDefaultText",
                            "EmitParticle",
                            "SetParticle",
                            "Checkpoint",
                            "AddParticle",
                            "SetInputEvent"));
        } else if (choice == 2) {
            effectsList.addAll(
                    Arrays.asList(
                            "AnimateTrack",
                            "MoveTrack",
                            "MoveDecoration",
                            "SetText",
                            "CustomBackground",
                            "Flash",
                            "SetFilter",
                            "HallOfMirrors",
                            "ShakeScreen",
                            "Bloom",
                            "ScreenTile",
                            "ScreenScroll",
                            "RepeatEvents",
                            "ConditionalEvents",
                            "AddDecoration",
                            "AddText",
                            "SetFrameRate",
                            "SetFloorIcon",
                            "SetObject",
                            "AddObject",
                            "SetFilterAdvanced",
                            "SetDefaultText",
                            "EmitParticle",
                            "SetParticle",
                            "AddParticle",
                            "SetInputEvent"));
        } else if (choice == 3) {
            effectsList.addAll(
                    Arrays.asList(
                            "AnimateTrack",
                            "MoveTrack",
                            "Flash",
                            "SetFilter",
                            "HallOfMirrors",
                            "ShakeScreen",
                            "Bloom",
                            "ScreenTile",
                            "ScreenScroll",
                            "SetFilterAdvanced",
                            "SetFrameRate"));
        } else if (choice == 4) {
            effectsList.addAll(
                    Arrays.asList(
                            "Flash",
                            "SetFilter",
                            "HallOfMirrors",
                            "ShakeScreen",
                            "Bloom",
                            "SetFilterAdvanced",
                            "SetFrameRate"));
        } else if (choice == 5) {
            effectsList.addAll(Arrays.asList("AddDecoration", "MoveDecoration"));
        }
        return effectsList;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                showToast("部分权限被拒绝，功能可能受限");
            }
        }
    }

    /*
        private void setupButtonListeners() {
            buttonCustomDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] selectedEffects = listViewEffects.getCheckedItemPositions();
                    ArrayList<String> selectedEffectsList = new ArrayList<>();
                    for (int i = 0; i < effectsList.size(); i++) {
                        if (selectedEffects[i] == 1) {
                            selectedEffectsList.add(effectsList.get(i));
                        }
                    }
                    if (!selectedEffectsList.isEmpty()) {
                        deleteCustomEffects(selectedEffectsList);
                    } else {
                        showToast("请选择要删除的特效");
                    }
                }
            });
        }

    */
    public class EffectsAdapter extends ArrayAdapter<String> {
        private SparseBooleanArray selectedItems = new SparseBooleanArray();
        private Context context;
        private ArrayList<String> effectsList;

        public EffectsAdapter(Context context, ArrayList<String> effectsList) {
            super(context, android.R.layout.simple_list_item_multiple_choice, effectsList);
            this.context = context;
            this.effectsList = effectsList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckedTextView textView;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView =
                        inflater.inflate(
                                android.R.layout.simple_list_item_multiple_choice, parent, false);
            }
            textView = (CheckedTextView) convertView.findViewById(android.R.id.text1);
            textView.setText(effectsList.get(position));
            boolean isChecked = selectedItems.get(position, false);
            textView.setChecked(isChecked);
            return convertView;
        }

        public void toggleSelection(int position) {
            if (selectedItems.get(position, false)) {
                selectedItems.delete(position);
            } else {
                selectedItems.put(position, true);
            }
            notifyDataSetChanged(); // 通知数据集改变
        }

        public SparseBooleanArray getSelectedItems() {
            return selectedItems;
        }
    }

    private void setupButtonListeners() {

        /*  buttonBackup.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        };});*/

        buttonDeleteAllEffects.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteEffects(1);
                        showLoadingDialog();
                    }
                });

        buttonDeleteMostEffects.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteEffects(2);
                        showLoadingDialog();
                    }
                });

        buttonDeleteVisualEffects.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteEffects(3);
                        showLoadingDialog();
                    }
                });

        buttonDeleteMajorFilters.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteEffects(4);
                        showLoadingDialog();
                    }
                });

        buttonDeleteDecorations.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteEffects(5);
                        showLoadingDialog();
                    }
                });
        /*
            buttonCustomDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String customEffects = editTextCustomEffects.getText().toString().trim();
                        if (!customEffects.isEmpty()) {
                            deleteCustomEffects(customEffects);
                        } else {
                            showToast("请输入要删除的自定义效果");
                        }
                    }
                });
        */
        buttonCustomDelete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SparseBooleanArray selectedPositions = effectsAdapter.getSelectedItems();
                        ArrayList<String> selectedEffects = new ArrayList<>();
                        for (int i = 0; i < selectedPositions.size(); i++) {
                            int key = selectedPositions.keyAt(i);
                            if (selectedPositions.get(key)) {
                                selectedEffects.add(effectsList.get(key));
                            }
                        }
                        if (!selectedEffects.isEmpty()) {
                            deleteCustomEffects(selectedEffects);
                        } else {
                            showToast("请选择要删除的特效");
                        }
                    }
                });
    }

    ProgressDialog progressDialog;

    // 在需要显示加载中窗口的地方调用该方法
    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("加载中");
        progressDialog.setMessage("请稍候...");
        progressDialog.setCancelable(false); // 设置对话框不可取消
        progressDialog.show();
    }

    // 在加载完成后调用该方法隐藏加载中窗口
    private void hideLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void deleteEffects(final int choice) {
        // showLoadingDialog(); // 在开始耗时操作前显示加载对话框

        executorService.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        String filePath = editTextFilePath.getText().toString().trim();
                        if (!filePath.isEmpty()) {
                            File file = new File(filePath);
                            if (file.exists()) {
                                try {
                                    // 备份文件
                                    /*        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                                    String backupFileName = "backup_" + dateFormat.format(new Date()) + "_" + file.getName();
                                    final File backupFile = new File(file.getParent(), backupFileName);
                                    Files.copy(file.toPath(), backupFile.toPath());*/

                                    // 创建新文件
                                    final File newFile =
                                            new File(
                                                    file.getParent(),
                                                    file.getName()
                                                            .replace(".adofai", "_effects.adofai"));
                                    BufferedWriter writer =
                                            new BufferedWriter(new FileWriter(newFile));

                                    BufferedReader reader =
                                            new BufferedReader(new FileReader(file));
                                    String line;
                                    ArrayList<String> effectsList = getEffectsList(choice);
                                    while ((line = reader.readLine()) != null) {
                                        boolean shouldDelete = false;
                                        for (String effect : effectsList) {
                                            if (line.contains(effect)) {
                                                shouldDelete = true;
                                                break;
                                            }
                                        }
                                        if (!shouldDelete) {
                                            writer.write(line);
                                            writer.newLine();
                                        }
                                    }
                                    reader.close();
                                    writer.close();

                                    // 在耗时操作完成后，使用runOnUiThread来更新UI
                                    runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    hideLoadingDialog(); // 关闭加载对话框
                                                    //  showToast("已成功删除效果并备份原文件: " +
                                                    // backupFile.getAbsolutePath());
                                                    showToast(
                                                            "生成的新文件: " + newFile.getAbsolutePath());
                                                }
                                            });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    showToast("删除效果时出错");
                                                    hideLoadingDialog();
                                                }
                                            });
                                }
                            } else {
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                showToast("文件不存在");
                                                hideLoadingDialog();
                                            }
                                        });
                            }
                        } else {
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            showToast("请输入文件路径");
                                            hideLoadingDialog();
                                        }
                                    });
                        }
                    }
                });
    }

    private void deleteCustomEffects(final ArrayList<String> customEffects) {
        executorService.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        String filePath = editTextFilePath.getText().toString().trim();
                        if (!filePath.isEmpty()) {
                            File file = new File(filePath);
                            if (file.exists()) {
                                try {
                                    // 创建新文件
                                    final File newFile =
                                            new File(
                                                    file.getParent(),
                                                    file.getName()
                                                            .replace(".adofai", "_effects.adofai"));
                                    BufferedWriter writer =
                                            new BufferedWriter(new FileWriter(newFile));

                                    BufferedReader reader =
                                            new BufferedReader(new FileReader(file));
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        boolean shouldDelete = false;
                                        for (String effect : customEffects) {
                                            if (line.contains(effect)) {
                                                shouldDelete = true;
                                                break;
                                            }
                                        }
                                        if (!shouldDelete) {
                                            writer.write(line);
                                            writer.newLine();
                                        }
                                    }
                                    reader.close();
                                    writer.close();

                                    // 在耗时操作完成后，使用runOnUiThread来更新UI
                                    runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    hideLoadingDialog(); // 关闭加载对话框
                                                    showToast(
                                                            "生成的新文件: " + newFile.getAbsolutePath());
                                                }
                                            });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    showToast("删除自定义效果时出错");
                                                    hideLoadingDialog();
                                                }
                                            });
                                }
                            } else {
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                showToast("文件不存在");
                                                hideLoadingDialog();
                                            }
                                        });
                            }
                        } else {
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            showToast("请输入文件路径");
                                            hideLoadingDialog();
                                        }
                                    });
                        }
                    }
                });
    }
}
