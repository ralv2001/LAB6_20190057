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

import com.example.lab6_20190057.adapters.MovimientosLimaPassAdapter;
import com.example.lab6_20190057.models.MovimientoLimaPass;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MovimientosLimaPassFragment extends Fragment implements MovimientosLimaPassAdapter.OnMovimientoClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private MaterialButton btnFilter;
    private FirebaseFirestore db;
    private String userId;
    private List<MovimientoLimaPass> movimientos;
    private MovimientosLimaPassAdapter adapter;

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
        movimientos = new ArrayList<>();
        adapter = new MovimientosLimaPassAdapter(movimientos);
        adapter.setOnMovimientoClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> {
            AddMovimientoLimaPassDialog dialog = new AddMovimientoLimaPassDialog();
            dialog.setOnMovimientoAddedListener(() -> loadMovimientos());
            dialog.show(getParentFragmentManager(), "AddMovimientoLimaPassDialog");
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
        System.out.println("🔄 CARGANDO MOVIMIENTOS LIMA PASS...");

        db.collection("movimientos_lima_pass")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("✅ FIRESTORE RESPUESTA EXITOSA - LIMA PASS");

                        List<MovimientoLimaPass> tempList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MovimientoLimaPass movimiento = document.toObject(MovimientoLimaPass.class);
                            movimiento.setId(document.getId());
                            tempList.add(movimiento);
                            System.out.println("📝 MOVIMIENTO LIMA PASS CARGADO: " + movimiento.getIdTarjeta());
                        }

                        System.out.println("📊 TOTAL MOVIMIENTOS LIMA PASS: " + tempList.size());
                        adapter.updateMovimientos(tempList);

                        if (tempList.isEmpty()) {
                            System.out.println("⚠️ NO HAY MOVIMIENTOS LIMA PASS");
                        }
                    } else {
                        System.out.println("❌ ERROR AL CARGAR LIMA PASS: " + task.getException().getMessage());
                    }
                });
    }

    @Override
    public void onEditClick(MovimientoLimaPass movimiento, int position) {
        EditMovimientoLimaPassDialog dialog = EditMovimientoLimaPassDialog.newInstance(movimiento);
        dialog.setOnMovimientoEditedListener(() -> {
            System.out.println("🔄 RECARGANDO DESPUÉS DE EDITAR LIMA PASS");
            loadMovimientos();
        });
        dialog.show(getParentFragmentManager(), "EditMovimientoLimaPassDialog");
    }

    @Override
    public void onDeleteClick(MovimientoLimaPass movimiento, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Movimiento")
                .setMessage("¿Estás seguro de que quieres eliminar este movimiento?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deleteMovimiento(movimiento, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteMovimiento(MovimientoLimaPass movimiento, int position) {
        db.collection("movimientos_lima_pass")
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