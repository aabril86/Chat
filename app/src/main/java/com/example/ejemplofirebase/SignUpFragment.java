package com.example.ejemplofirebase;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ejemplofirebase.databinding.FragmentSignInBinding;
import com.example.ejemplofirebase.databinding.FragmentSignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.UUID;

public class SignUpFragment extends Fragment {

    private SignUpViewModel vm;

    public static class SignUpViewModel extends ViewModel{
        Uri uri;
    }

    private FragmentSignUpBinding binding;
    private NavController navController;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentSignUpBinding.inflate(inflater, container, false)).getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        mAuth = FirebaseAuth.getInstance();
        vm = new ViewModelProvider(this).get(SignUpViewModel.class);
        storage = FirebaseStorage.getInstance();


        binding.emailSignUp.setOnClickListener(v -> {
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();
            String name = binding.nombre.getText().toString();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    storage.getReference("avatars/*" + UUID.randomUUID())
                            .putFile(vm.uri)
                            .continueWithTask(task1 -> task1.getResult().getStorage().getDownloadUrl())
                            .addOnSuccessListener(url -> {
                                mAuth.getCurrentUser()
                                        .updateProfile(
                                                new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(name)
                                                        .setPhotoUri(url)
                                                        .build());
                            });
                    navController.navigate(R.id.action_signUpFragment_to_chatFragment);
                }else {
                    Toast.makeText(requireContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.foto.setOnClickListener(v -> {
            galeria.launch("image/*");
        });

        if(vm.uri != null) Glide.with(requireView()).load(vm.uri).into(binding.foto);
    }

    ActivityResultLauncher<String> galeria = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        vm.uri = uri;
        Glide.with(requireView()).load(uri).into(binding.foto);
    });
}