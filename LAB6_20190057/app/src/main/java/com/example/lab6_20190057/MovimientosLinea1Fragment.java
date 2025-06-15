package com.example.lab6_20190057;

import android.app.AlertDialog;
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

import com.example.lab6_20190057.adapters.MovimientosLinea1Adapter;
import com.example.lab6_20190057.models.MovimientoLinea1;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MovimientosLinea1Fragment extends Fragment implements MovimientosLinea1Adapter.OnMovimientoClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private MaterialButton btnFilter;
    private FirebaseFirestore db;
    private String userId;
    private List<MovimientoLinea1> movimientos;
    private MovimientosLinea1Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movimientos_linea1, container, false);

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
        adapter = new MovimientosLinea1Adapter(movimientos);
        adapter.setOnMovimientoClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> {
            AddMovimientoLinea1Dialog dialog = new AddMovimientoLinea1Dialog();
            dialog.setOnMovimientoAddedListener(() -> loadMovimientos());
            dialog.show(getParentFragmentManager(), "AddMovimientoLinea1Dialog");
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
                    // Aquí implementarías el filtrado real
                    Toast.makeText(getContext(), "Filtrar por: " + dayOfMonth + "/" + (month + 1) + "/" + year, Toast.LENGTH_SHORT).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadMovimientos() {
        System.out.println("🔄 CARGANDO MOVIMIENTOS...");

        db.collection("movimientos_linea1")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("✅ FIRESTORE RESPUESTA EXITOSA");
                        movimientos.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MovimientoLinea1 movimiento = document.toObject(MovimientoLinea1.class);
                            movimiento.setId(document.getId());
                            movimientos.add(movimiento);
                            System.out.println("📝 MOVIMIENTO CARGADO: " + movimiento.getIdTarjeta());
                        }

                        System.out.println("📊 TOTAL MOVIMIENTOS: " + movimientos.size());
                        adapter.updateMovimientos(movimientos);

                        if (movimientos.isEmpty()) {
                            System.out.println("⚠️ NO HAY MOVIMIENTOS");
                        }
                    } else {
                        System.out.println("❌ ERROR AL CARGAR: " + task.getException().getMessage());
                    }
                });
    }

    @Override
    public void onEditClick(MovimientoLinea1 movimiento, int position) {
        // TODO: Implementar edición
        Toast.makeText(getContext(), "Editar movimiento: " + movimiento.getIdTarjeta(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(MovimientoLinea1 movimiento, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Movimiento")
                .setMessage("¿Estás seguro de que quieres eliminar este movimiento?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deleteMovimiento(movimiento, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteMovimiento(MovimientoLinea1 movimiento, int position) {
        db.collection("movimientos_linea1")
                .document(movimiento.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    adapter.removeMovimiento(position);
                    Toast.makeText(getContext(), "Movimiento eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}