package xyz.hdk5.cunnyarchive

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.STORAGE_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.common.io.ByteStreams
import xyz.hdk5.cunnyarchive.ui.theme.CunnyArchiveTheme
import java.io.File

const val APP_TAG = "CunnyArchive"
const val MX_PACKAGE_NAME = "com.nexon.bluearchive"
const val MX_DATA_PATH = "Android/data/$MX_PACKAGE_NAME"
const val MX_DOCID = "primary:$MX_DATA_PATH"
const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
val MX_URI = DocumentsContract.buildDocumentUri(
    EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
    MX_DOCID,
)
val MX_TREE_URI = DocumentsContract.buildTreeDocumentUri(
    EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
    MX_DOCID,
)
val CENSOR_URI_MAP = mapOf(
    "files/PUB/Resource/GameData/MediaResources/UIs/03_Scenario/01_Background/BG_CS_Millenium_01_c.png" to "files/PUB/Resource/GameData/MediaResources/UIs/03_Scenario/01_Background/BG_CS_Millenium_01.jpg",
    "files/PUB/Resource/GameData/MediaResources/UIs/03_Scenario/01_Background/BG_CS_Millenium_10_c.png" to "files/PUB/Resource/GameData/MediaResources/UIs/03_Scenario/01_Background/BG_CS_Millenium_10.jpg",
).entries.associate {
    makeMxTreeUri(it.key) to makeMxTreeUri(it.value)
}

@RequiresApi(Build.VERSION_CODES.Q)
fun makeMxTreeUri29(path: String): Uri {
    val docid = "$MX_DOCID/$path"
    val uri = DocumentsContract.buildDocumentUriUsingTree(
        MX_TREE_URI,
        docid,
    )
    return uri
}

fun makeMxTreeUriLegacy(path: String): Uri {
    val storagePath = Environment.getExternalStorageDirectory().absolutePath
    return Uri.fromFile(File("$storagePath/$MX_DATA_PATH/$path"))
}

fun makeMxTreeUri(path: String): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        makeMxTreeUri29(path)
    } else {
        makeMxTreeUriLegacy(path)
    }
}

fun copyUncensored(context: Context) {
    val contentResolver = context.contentResolver

    for ((censoredUri, uncensoredUri) in CENSOR_URI_MAP) {
        contentResolver.openInputStream(uncensoredUri).use { inputStream ->
            contentResolver.openOutputStream(censoredUri, "wt")
                .use { outputStream ->
                    ByteStreams.copy(inputStream!!, outputStream!!)
                    Log.d(APP_TAG, "Copied $uncensoredUri to $censoredUri")
                }
        }
    }
}

fun deleteByUri(context: Context, uri: Uri): Boolean {
    val contentResolver = context.contentResolver
    return when (uri.scheme) {
        "file" -> {
            val file = File(uri.path!!)
            file.delete()
        }
        "content" -> DocumentsContract.deleteDocument(contentResolver, uri)
        else -> false
    }
}

fun deleteCensored(context: Context) {
    for ((censoredUri, _) in CENSOR_URI_MAP) {
        try {
            deleteByUri(context, censoredUri)
            Log.d(APP_TAG, "Removed: $censoredUri")
        } catch (e: IllegalArgumentException) {
            Log.d(APP_TAG, "Not Found: $censoredUri")
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CunnyArchiveTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainComposable()
                }
            }
        }
    }
}

@Composable
fun OpenDirectoryButtonCommon(
    onClick: () -> Unit,
) {
    Button(onClick = onClick) {
        Text(text = "Request Storage Access")
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun OpenDirectoryButton29(onResult: () -> Unit) {
    val context = LocalContext.current

    val storageManager = context.getSystemService(STORAGE_SERVICE) as StorageManager
    val primaryStorageVolume = storageManager.primaryStorageVolume
    val intent = primaryStorageVolume.createOpenDocumentTreeIntent().apply {
        putExtra(
            DocumentsContract.EXTRA_INITIAL_URI,
            MX_URI,
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.data?.let { directoryUri ->
                context.contentResolver.takePersistableUriPermission(
                    directoryUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                Log.d(APP_TAG, "Taken r/w permission for $directoryUri")
            }
        }

        onResult()
    }

    OpenDirectoryButtonCommon(
        onClick = {
            launcher.launch(intent)
        }
    )
}

@Composable
fun OpenDirectoryButtonLegacy(onResult: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        onResult()
    }

    OpenDirectoryButtonCommon(
        onClick = {
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    )
}

@Composable
fun OpenDirectoryButton() {
    val context = LocalContext.current
    val onResult = {
        Toast.makeText(context, "Permission acquired", Toast.LENGTH_LONG).show()
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        OpenDirectoryButton29(onResult)
    } else {
        OpenDirectoryButtonLegacy(onResult)
    }
}

@Composable
fun UncensorButton() {
    val context = LocalContext.current

    Button(onClick = {
        copyUncensored(context)
        Toast.makeText(context, "Uncensoring completed", Toast.LENGTH_LONG).show()
    }) {
        Text(text = "Uncensor")
    }
}

@Composable
fun RecensorButton() {
    val context = LocalContext.current
    Button(onClick = {
        deleteCensored(context)
        Toast.makeText(context, "Recensoring completed", Toast.LENGTH_LONG).show()
    }) {
        Text("Recensor")
    }
}

@Composable
fun MainComposable() {
    Column {
        Text(stringResource(R.string.open_directory_note))
        OpenDirectoryButton()
        Text(stringResource(R.string.uncensor_note))
        UncensorButton()
        Text(stringResource(R.string.recensor_note))
        RecensorButton()
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MainComposable()
}
