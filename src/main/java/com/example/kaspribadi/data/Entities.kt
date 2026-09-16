package com.example.kaspribadi.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nama: String,
    val jenisInOut: String // "IN" atau "OUT"
)

@Entity(
    tableName = "transaction_table",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val nominal: Double,
    val tanggal: String,
    val catatan: String
)

@Entity(tableName = "savings_goal")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetNama: String,
    val targetNominal: Double,
    val terkumpul: Double = 0.0
)