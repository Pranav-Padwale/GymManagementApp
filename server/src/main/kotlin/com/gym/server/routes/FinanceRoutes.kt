package com.gym.server.routes

import com.gym.model.finance.Transaction
import com.gym.model.finance.TransactionCategory
import com.gym.model.finance.TransactionType
import com.gym.server.database.MongoConfig
import com.gym.server.documents.TransactionDocument
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Route.financeRoutes() {
    authenticate {
        route("/api/transactions") {
            get {
                // RBAC Check: Only SUPER_ADMIN can view finances
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                
                if (role != "SUPER_ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "Access Denied: Only Super Admins can view finances.")
                    return@get
                }
                
                val docs = MongoConfig.transactions.find().toList()
                val transactions = docs.map { it.toSharedModel() }
                call.respond(transactions)
            }
            
            post {
                // RBAC Check: Only SUPER_ADMIN can add finances
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                
                if (role != "SUPER_ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "Access Denied: Only Super Admins can add finances.")
                    return@post
                }
                
                val transaction = call.receive<Transaction>()
                val doc = transaction.toDocument()
                MongoConfig.transactions.insertOne(doc)
                call.respond(HttpStatusCode.Created, transaction)
            }
        }
    }
}

// Extensions
fun TransactionDocument.toSharedModel() = Transaction(
    id = id,
    title = title,
    amount = amount,
    category = TransactionCategory.valueOf(category),
    type = TransactionType.valueOf(type),
    dateIso = dateIso,
    year = year,
    month = month
)

fun Transaction.toDocument() = TransactionDocument(
    id = id,
    title = title,
    amount = amount,
    category = category.name,
    type = type.name,
    dateIso = dateIso,
    year = year,
    month = month
)
