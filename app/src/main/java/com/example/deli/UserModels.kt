package com.example.deli

data class UserCreateRequest(
    val email: String,
    val password: String,
    val first_name: String,
    val last_name: String,
    val link: String? = null
)

data class UserLoginRequest(
    val email: String,
    val password: String
)

data class RegisterResponse(
    val status: String,
    val user_id: String,
    val message: String
)

data class FriendSendRequest(
    val user_id: String,
    val friend_id: String
)

data class FriendRespondRequest(
    val user_id: String,
    val friend_id: String,
    val action: String
)

data class FriendUser(
    val user_id: String,
    val email: String,
    val first_name: String,
    val last_name: String
)

data class OkResponse(
    val ok: Boolean
)

data class EventCreateRequest(
    val creator_id: String,
    val title: String,
    val deadline: String? = null,
    val participant_ids: List<String> = emptyList(),
    val guest_names: List<String> = emptyList()
)

data class EventResponse(
    val id: String,
    val creator_id: String,
    val title: String,
    val deadline: String? = null,
    val status: String,
    val created_at: String? = null,
    val participants: List<EventParticipant>? = null,
    val balances: List<BalanceItem>? = null
)

data class EventParticipant(
    val user_id: String,
    val first_name: String,
    val last_name: String
)

data class ParticipantAddRequest(
    val user_id: String
)

data class GuestCreateRequest(
    val name: String
)

data class PurchaseUpdateRequest(
    val description: String? = null,
    val amount: Double? = null,
    val beneficiary_ids: List<String>? = null
)

data class PurchaseCreateRequest(
    val buyer_id: String,
    val description: String,
    val amount: Double,
    val receipt_photo_url: String? = null,
    val beneficiary_ids: List<String> = emptyList()
)

data class PurchaseResponse(
    val id: String,
    val event_id: String? = null,
    val buyer: EventParticipant? = null,
    val description: String,
    val amount: Double,
    val receipt_photo_url: String? = null,
    val created_at: String? = null,
    val beneficiaries: List<BeneficiaryItem>? = null
)

data class BeneficiaryItem(
    val user_id: String,
    val first_name: String,
    val last_name: String,
    val share_amount: Double
)

data class BalanceItem(
    val user_id: String,
    val first_name: String,
    val last_name: String,
    val balance: Double
)

data class SettlementItem(
    val from: BalanceItem,
    val to: BalanceItem,
    val amount: Double
)

data class DebtCreateRequest(
    val creditor_id: String,
    val debtor_id: String,
    val amount: Double,
    val description: String? = null,
    val deadline: String? = null,
    val photo_url: String? = null
)

data class DebtUpdateRequest(
    val status: String,
    val payment_photo_url: String? = null
)

data class DebtResponse(
    val id: String,
    val creditor: EventParticipant? = null,
    val debtor: EventParticipant? = null,
    val amount: Double,
    val description: String? = null,
    val deadline: String? = null,
    val photo_url: String? = null,
    val payment_photo_url: String? = null,
    val status: String,
    val created_at: String? = null
)

data class SummaryItem(
    val type: String,
    val counterparty: EventParticipant? = null,
    val amount: Double,
    val description: String? = null,
    val deadline: String? = null,
    val debt_id: String? = null,
    val event_id: String? = null,
    val event_title: String? = null,
    val debtors: List<BalanceItem>? = null,
    val creditors: List<BalanceItem>? = null,
    val photo_url: String? = null
)

data class UploadResponse(
    val url: String
)

data class UserUpdateRequest(
    val first_name: String? = null,
    val last_name: String? = null,
    val link: String? = null
)

data class UserResponse(
    val user_id: String,
    val email: String? = null,
    val first_name: String,
    val last_name: String? = null,
    val link: String? = null
)
