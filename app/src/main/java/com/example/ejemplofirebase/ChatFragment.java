package com.example.ejemplofirebase;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.ejemplofirebase.databinding.FragmentChatBinding;
import com.example.ejemplofirebase.databinding.FragmentSignInBinding;
import com.example.ejemplofirebase.databinding.ViewholderMensajeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private NavController navController;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private List<Mensaje> chat = new ArrayList<>();
    private String COLLECCION_MENSAJES = "mensaje";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (binding = FragmentChatBinding.inflate(inflater, container, false)).getRoot();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        ChatAdapter chatAdapter = new ChatAdapter();
        binding.chat.setAdapter(chatAdapter);

        binding.enviar.setOnClickListener(v -> {
            String texto = binding.mensaje.getText().toString();
            String fecha = LocalDateTime.now().toString();
            String email = mAuth.getCurrentUser().getEmail();
            String nombre = mAuth.getCurrentUser().getDisplayName();
            String foto = mAuth.getCurrentUser().getPhotoUrl().toString();

            mDb.collection("mensaje").add(new Mensaje(texto, fecha, email ,nombre, foto));
        });

        binding.adjuntar.setOnClickListener(v -> {
            galeria.launch("image/*");
        });

        mDb.collection("mensaje").orderBy("fecha").addSnapshotListener((value, error) -> {
                chat.clear();
                value.forEach(document -> {
                    chat.add(new Mensaje(
                    document.getString("mensaje"),
                    document.getString("fecha"),
                    document.getString("email"),
                    document.getString("nombre"),
                    document.getString("foto"),
                    document.getString("meme")));
                    });
                chatAdapter.notifyDataSetChanged();
                binding.chat.scrollToPosition(chat.size()-1);
        });


    }

    class ChatAdapter extends RecyclerView.Adapter<MensajeViewHolder>{

        @NonNull
        @Override
        public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MensajeViewHolder(ViewholderMensajeBinding.inflate(getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MensajeViewHolder holder, int position) {

            Mensaje mensaje = chat.get(position);

            holder.binding.nombre.setText(mensaje.nombre);
            if(mensaje.meme != null){
                Glide.with(requireView()).load(mensaje.meme).into(holder.binding.meme);
                holder.binding.mensaje.setVisibility(View.GONE);
                holder.binding.meme.setVisibility(View.VISIBLE);
            }else{
                holder.binding.mensaje.setText(mensaje.mensaje);
                holder.binding.mensaje.setVisibility(View.VISIBLE);
                holder.binding.meme.setVisibility(View.GONE);
            }

            holder.binding.fecha.setText(mensaje.fecha);
            Glide.with(requireView()).load(mensaje.foto).into(holder.binding.foto);

            if(mensaje.nombre.equals(mAuth.getCurrentUser().getEmail())){
                holder.binding.getRoot().setGravity(Gravity.END);
            }else{
                holder.binding.getRoot().setGravity(Gravity.START);
            }
        }

        @Override
        public int getItemCount() {
            return chat.size();
        }
    }


    static class MensajeViewHolder extends RecyclerView.ViewHolder{
        ViewholderMensajeBinding binding;
        public MensajeViewHolder(@NonNull ViewholderMensajeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private Uri uri;
    ActivityResultLauncher<String> galeria = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        FirebaseStorage.getInstance().getReference("imagenes/"+ UUID.randomUUID())
                .putFile(uri).continueWithTask(task -> task.getResult().getStorage().getDownloadUrl())
                .addOnSuccessListener(url -> {
                    mDb.collection("mensaje").add(new Mensaje("",
                            LocalDateTime.now().toString(),
                            mAuth.getCurrentUser().getDisplayName(),
                            mAuth.getCurrentUser().getEmail(),
                            mAuth.getCurrentUser().getPhotoUrl().toString(),
                            url.toString()));
                });

    });
}