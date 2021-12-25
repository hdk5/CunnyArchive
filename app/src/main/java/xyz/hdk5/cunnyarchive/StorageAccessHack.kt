package xyz.hdk5.cunnyarchive

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable


@RequiresApi(Build.VERSION_CODES.Q)
open class OpenDocumentTreeQWorkaround : ActivityResultContracts.OpenDocumentTree() {
    @SuppressLint("MissingSuperCall")
    override fun createIntent(context: Context, input: Uri?): Intent {
        // Huge thanks to https://stackoverflow.com/a/66466205 for this workaround
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val primaryStorageVolume = storageManager.primaryStorageVolume
        val intent = primaryStorageVolume.createOpenDocumentTreeIntent()
        if (input != null) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, input)
        }
        return intent
    }
}

object makeLaunchFunction {
    @Composable
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun invokeQ(onResult: () -> Unit): () -> Unit {
        val launcher = rememberLauncherForActivityResult(
            contract = OpenDocumentTreeQWorkaround(),
        ) {
            onResult()
        }

        return {
            launcher.launch(MX_URI)
        }
    }

    @Composable
    private fun invokeLegacy(onResult: () -> Unit): () -> Unit {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) {
            onResult()
        }

        return {
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    @Composable
    operator fun invoke(onResult: () -> Unit): () -> Unit {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> invokeQ(onResult)
            else -> invokeLegacy(onResult)
        }
    }
}
