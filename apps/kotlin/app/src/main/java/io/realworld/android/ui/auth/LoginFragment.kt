package io.realworld.android.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import io.realworld.android.AuthStatus
import io.realworld.android.AuthViewModel
import io.realworld.android.R
import io.realworld.android.databinding.FragmentLoginSignupBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginSignupBinding? = null
    val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginSignupBinding.inflate(inflater, container, false)
        _binding?.usernameEditText?.isVisible = false

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.apply {
            submitButton.setOnClickListener {
                authViewModel.login(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }

        // assignment2: Snackbar-based authStatus observer for error feedback
        authViewModel.authStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is AuthStatus.Success -> {
                    _binding?.submitButton?.isEnabled = true
                    authViewModel.resetAuthStatus()
                }

                is AuthStatus.Error -> {
                    _binding?.submitButton?.isEnabled = true
                    _binding?.root?.let {
                        Snackbar.make(it, status.msg, Snackbar.LENGTH_LONG).show()
                    }
                    authViewModel.resetAuthStatus()
                }

                is AuthStatus.Idle -> {
                    _binding?.submitButton?.isEnabled = true
                }
            }
        }

        // assignment3: Navigate to feed on successful login
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
