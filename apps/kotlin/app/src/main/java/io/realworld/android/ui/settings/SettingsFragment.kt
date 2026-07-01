package io.realworld.android.ui.settings

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import io.realworld.android.AuthViewModel
import io.realworld.android.data.UserRepo
import io.realworld.android.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch
import java.io.File

class SettingsFragment : Fragment() {
    companion object {
        private const val REQUEST_PICK_IMAGE = 1001
        private const val REQUEST_TAKE_PHOTO = 1002
    }

    private var _binding: FragmentSettingsBinding? = null
    private val authViewModel by activityViewModels<AuthViewModel>()
    private var selectedImageFile: File? = null
    private var currentImagePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return _binding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.user.observe(viewLifecycleOwner) {
            _binding?.apply {
                bioEditText.setText(it?.bio ?: "")
                emailEditText.setText(it?.email ?: "")
                usernameEditText.setText(it?.username ?: "")
                currentImagePath = it?.image
                if (selectedImageFile == null) {
                    imageEditText.setText(it?.image ?: "")
                }
            }
        }

        _binding?.apply {
            chooseImageButton.setOnClickListener {
                openGalleryPicker()
            }

            takePhotoButton.setOnClickListener {
                openCamera()
            }

            submitButton.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val uploadedImageUrl =
                        selectedImageFile?.let { imageFile ->
                            UserRepo.uploadProfileImage(imageFile)
                        }

                    if (selectedImageFile != null && uploadedImageUrl == null) {
                        Toast.makeText(requireContext(), "Image upload failed.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    authViewModel.update(
                        bio = bioEditText.text.toString(),
                        username = usernameEditText.text.toString().takeIf { it.isNotBlank() },
                        image = uploadedImageUrl ?: currentImagePath,
                        email = emailEditText.text.toString().takeIf { it.isNotBlank() },
                        password = passwordEditText.text.toString().takeIf { it.isNotBlank() },
                    )

                    selectedImageFile = null
                    Toast.makeText(requireContext(), "Profile saved.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openGalleryPicker() {
        val intent =
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        @Suppress("DEPRECATION")
        startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_PICK_IMAGE)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_TAKE_PHOTO)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != android.app.Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_PICK_IMAGE -> {
                val uri = data?.data ?: return
                val copiedFile = copyUriToCache(uri)
                if (copiedFile == null) {
                    Toast.makeText(requireContext(), "Loading image failed.", Toast.LENGTH_SHORT).show()
                    return
                }
                selectedImageFile = copiedFile
                _binding?.imageEditText?.setText(copiedFile.absolutePath)
            }

            REQUEST_TAKE_PHOTO -> {
                val bitmap = data?.extras?.get("data") as? Bitmap ?: return
                val newImage = saveBitmapToCache(bitmap) ?: return
                selectedImageFile = newImage
                _binding?.imageEditText?.setText(newImage.absolutePath)
            }
        }
    }

    private fun copyUriToCache(uri: Uri): File? {
        val context = requireContext()
        val extension =
            context.contentResolver.getType(uri)?.let {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
            } ?: "jpg"

        val outputFile =
            File(
                File(context.cacheDir, "profile_images").apply { mkdirs() },
                "selected_${System.currentTimeMillis()}.$extension",
            )

        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outputFile
        } catch (e: Exception) {
            null
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): File? {
        val outputFile =
            File(
                File(requireContext().cacheDir, "profile_images").apply { mkdirs() },
                "camera_${System.currentTimeMillis()}.jpg",
            )

        return try {
            outputFile.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
            }
            outputFile
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
