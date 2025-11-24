# Complete API Reference - Correct Endpoints

## 🚨 Current Frontend Issues

Your frontend is calling **non-existent endpoints**:

```
❌ PUT /api/orders/1/delivery/status  (DOES NOT EXIST)
❌ PUT /api/orders/1/payment/status   (DOES NOT EXIST)
```

## ✅ Correct API Endpoints

### 1. Order Status Management
```
PUT /api/orders/{orderId}/status
```
**Purpose:** Update order business status  
**Valid Statuses:** `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `COMPLETED`, `CANCELLED`

**Example:**
```javascript
await api.put('/api/orders/1/status', { status: 'CONFIRMED' });
```

---

### 2. Delivery Status Management
```
GET /api/deliveries/order/{orderId}    // Get delivery by order ID
PUT /api/deliveries/{deliveryId}/status // Update delivery status
```
**Purpose:** Update logistics/delivery tracking  
**Valid Statuses:** `PENDING`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `COLLECTED`, `READY_FOR_COLLECTION`, `FAILED_DELIVERY`, `RETURNED`

**Example:**
```javascript
// Step 1: Get delivery info
const delivery = await api.get('/api/deliveries/order/1');

// Step 2: Update delivery status
await api.put(`/api/deliveries/${delivery.data.deliveryId}/status`, { 
    status: 'OUT_FOR_DELIVERY' 
});
```

---

### 3. Payment Status Management
```
GET /api/payments/order/{orderId}    // Get payment by order ID
PUT /api/payments/{paymentId}/status // Update payment status
```
**Purpose:** Update payment processing status  
**Valid Statuses:** `PENDING`, `CONFIRMED`, `COMPLETED`, `FAILED`, `REFUNDED`

**Example:**
```javascript
// Step 1: Get payment info
const payment = await api.get('/api/payments/order/1');

// Step 2: Update payment status
await api.put(`/api/payments/${payment.data.paymentId}/status`, { 
    status: 'CONFIRMED' 
});
```

---

## 🔧 Frontend Code Fixes

### Current Broken Code:
```javascript
// ❌ These functions are calling wrong endpoints
async function updateOrderDeliveryStatus(orderId, status) {
    return await api.put(`/api/orders/${orderId}/delivery/status`, { status });
}

async function updateOrderPaymentStatus(orderId, status) {
    return await api.put(`/api/orders/${orderId}/payment/status`, { status });
}
```

### Fixed Code:
```javascript
// ✅ Correct delivery status update
async function updateDeliveryStatus(orderId, status) {
    try {
        // Get delivery ID first
        const delivery = await api.get(`/api/deliveries/order/${orderId}`);
        
        // Update delivery status
        return await api.put(`/api/deliveries/${delivery.data.deliveryId}/status`, { 
            status 
        });
    } catch (error) {
        console.error('Error updating delivery status:', error);
        throw error;
    }
}

// ✅ Correct payment status update
async function updatePaymentStatus(orderId, status) {
    try {
        // Get payment ID first
        const payment = await api.get(`/api/payments/order/${orderId}`);
        
        // Update payment status
        return await api.put(`/api/payments/${payment.data.paymentId}/status`, { 
            status 
        });
    } catch (error) {
        console.error('Error updating payment status:', error);
        throw error;
    }
}

// ✅ Order status update (this one was probably correct)
async function updateOrderStatus(orderId, status) {
    return await api.put(`/api/orders/${orderId}/status`, { status });
}
```

---

## 🎯 Complete Status Workflow

### Typical Order Flow:
```javascript
// 1. Update order status
await updateOrderStatus(orderId, 'CONFIRMED');

// 2. Update payment status  
await updatePaymentStatus(orderId, 'CONFIRMED');

// 3. Update delivery status
await updateDeliveryStatus(orderId, 'OUT_FOR_DELIVERY');

// 4. When delivered
await updateDeliveryStatus(orderId, 'DELIVERED');
// ↳ This automatically updates order to 'COMPLETED' and enables reviews!
```

---

## 📋 Status Value Reference

### Order Status Values:
- `PENDING` - Order created, awaiting payment
- `CONFIRMED` - Payment received, order confirmed  
- `PROCESSING` - Order being prepared
- `SHIPPED` - Order shipped/ready for pickup
- `COMPLETED` - Order completed (enables reviews)
- `CANCELLED` - Order cancelled

### Delivery Status Values:
- `PENDING` - Awaiting dispatch
- `IN_TRANSIT` - On the way
- `OUT_FOR_DELIVERY` - Out for delivery
- `DELIVERED` - Successfully delivered → **Auto-updates order to COMPLETED**
- `COLLECTED` - Picked up from store → **Auto-updates order to COMPLETED**
- `READY_FOR_COLLECTION` - Ready for pickup
- `FAILED_DELIVERY` - Delivery failed
- `RETURNED` - Returned to sender

### Payment Status Values:
- `PENDING` - Payment pending
- `CONFIRMED` - Payment confirmed
- `COMPLETED` - Payment completed
- `FAILED` - Payment failed
- `REFUNDED` - Payment refunded

---

## 🚀 Automatic Features

### When delivery becomes `DELIVERED` or `COLLECTED`:
1. ✅ Order status automatically updates to `COMPLETED`
2. ✅ Reviews become available immediately
3. ✅ No manual order status update needed
4. ✅ Real-time frontend updates should reflect changes

### Benefits:
- **No complex status synchronization code needed**
- **Automatic review enablement**
- **Consistent business logic**
- **Error-free status management**

---

## 🛠️ Quick Fix Checklist

Replace in your frontend:

- [ ] **Remove:** `/api/orders/{id}/delivery/status` endpoints
- [ ] **Remove:** `/api/orders/{id}/payment/status` endpoints  
- [ ] **Add:** Get delivery/payment by order ID calls
- [ ] **Add:** Update delivery/payment by their own IDs
- [ ] **Update:** All status dropdown values to match backend enums
- [ ] **Test:** Complete order flow from creation to delivery
- [ ] **Verify:** Reviews automatically become available

This will fix all your 403 Forbidden errors and make the system work perfectly!