package io.realworld.android.ui.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.realworld.android.AuthViewModel
import io.realworld.android.databinding.FragmentLoginSignupBinding

class SignupFragment : Fragment() {
    private var _binding: FragmentLoginSignupBinding? = null
    val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentLoginSignupBinding.inflate(inflater, container, false)
        _binding?.usernameEditText?.isVisible = true

        return _binding?.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.apply {
            submitButton.setOnClickListener {
                val username = usernameEditText.text.toString()
                if (username.contains('/')) {
                    errorTextView.text = "No '/' allowed in usernames."
                    errorTextView.isVisible = true
                    return@setOnClickListener
                }

                errorTextView.isVisible = false
                authViewModel.signup(
                    username,
                    emailEditText.text.toString(),
                    passwordEditText.text.toString(),
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
