package com.example.lab6_20190057;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20190057.models.MovimientoLimaPass;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MovimientosLimaPassFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private MaterialButton btnFilter;
    private FirebaseFirestore db;
    private String userId;
    private List<MovimientoLimaPass> movimientos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movimientos_lima_pass, container, false);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadMovimientos();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewMovimientos);
        fabAdd = view.findViewById(R.id.fabAddMovimiento);
        btnFilter = view.findViewById(R.id.btnFilter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        movimientos = new ArrayList<>();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Agregar movimiento Lima Pass", Toast.LENGTH_SHORT).show();
        });

        btnFilter.setOnClickListener(v -> {
            showDatePicker();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    Toast.makeText(getContext(), "Filtrar por: " + dayOfMonth + "/" + (month + 1) + "/" + year, Toast.LENGTH_SHORT).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadMovimientos() {
        db.collection("movimientos_lima_pass")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        movimientos.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MovimientoLimaPass movimiento = document.toObject(MovimientoLimaPass.class);
                            movimiento.setId(document.getId());
                            movimientos.add(movimiento);
                        }
                        Toast.makeText(getContext(), "Cargados " + movimientos.size() + " movimientos", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error al cargar movimientos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}