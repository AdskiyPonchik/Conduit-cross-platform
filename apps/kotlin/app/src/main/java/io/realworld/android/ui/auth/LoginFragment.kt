package io.realworld.android.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import io.realworld.android.AuthViewModel
import io.realworld.android.R
import io.realworld.android.databinding.FragmentLoginSignupBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginSignupBinding? = null
    val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentLoginSignupBinding.inflate(inflater, container, false)
        _binding?.usernameEditText?.isVisible = false

        return _binding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.apply {
            submitButton.setOnClickListener {
                errorTextView.isVisible = false
                authViewModel.login(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString(),
                )
            }
        }

        authViewModel.loginError.observe(viewLifecycleOwner) { errorMsg ->
            _binding?.errorTextView?.apply {
                if (errorMsg != null) {
                    text = errorMsg
                    isVisible = true
                } else {
                    isVisible = false
                }
            }
        }

        authViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
                    .navigate(R.id.nav_feed)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
