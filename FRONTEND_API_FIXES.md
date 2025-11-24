# ⚠️ CRITICAL Frontend API Fixes Required

## 🚨 Immediate Issues Found

Based on the error logs, there are **critical API endpoint and status value mistakes** in the frontend that need immediate fixes.

---

## ❌ Problem 1: Wrong API Endpoint

**Frontend is calling:**
```
PUT /api/orders/1/delivery/status
```

**This endpoint DOES NOT EXIST!**

**Correct endpoint:**
```
PUT /api/deliveries/{deliveryId}/status
```

### Fix Required:
```javascript
// ❌ WRONG - Don't use this
async function updateOrderDeliveryStatus(orderId, status) {
    return await api.put(`/api/orders/${orderId}/delivery/status`, { status });
}

// ✅ CORRECT - Use this instead
async function updateDeliveryStatus(deliveryId, status) {
    return await api.put(`/api/deliveries/${deliveryId}/status`, { status });
}
```

---

## ❌ Problem 2: Wrong Status Value

**Frontend is sending:**
```json
{ "status": "CONFIRMED" }
```

**But `CONFIRMED` is an ORDER STATUS, not a DELIVERY STATUS!**

### Valid Delivery Status Values:
- `PENDING`
- `IN_TRANSIT`
- `OUT_FOR_DELIVERY`
- `DELIVERED`
- `COLLECTED`
- `READY_FOR_COLLECTION`
- `FAILED_DELIVERY`
- `RETURNED`

### Valid Order Status Values:
- `PENDING`
- `CONFIRMED` ⚠️ (This is Order only!)
- `PROCESSING`
- `SHIPPED`
- `COMPLETED`
- `CANCELLED`

---

## 🔧 Complete API Reference

### 1. Update Order Status
```javascript
// Endpoint: PUT /api/orders/{orderId}/status
// Valid values: PENDING, CONFIRMED, PROCESSING, SHIPPED, COMPLETED, CANCELLED

async function updateOrderStatus(orderId, orderStatus) {
    return await api.put(`/api/orders/${orderId}/status`, { 
        status: orderStatus // e.g., "CONFIRMED", "COMPLETED"
    });
}

// Example usage:
await updateOrderStatus(1, "CONFIRMED"); // ✅ Correct
```

### 2. Update Delivery Status
```javascript
// Endpoint: PUT /api/deliveries/{deliveryId}/status
// Valid values: PENDING, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, COLLECTED, etc.

async function updateDeliveryStatus(deliveryId, deliveryStatus) {
    return await api.put(`/api/deliveries/${deliveryId}/status`, { 
        status: deliveryStatus // e.g., "DELIVERED", "COLLECTED"
    });
}

// Example usage:
await updateDeliveryStatus(5, "DELIVERED"); // ✅ Correct
```

### 3. Get Delivery by Order ID
```javascript
// First get delivery ID from order
async function getDeliveryByOrderId(orderId) {
    return await api.get(`/api/deliveries/order/${orderId}`);
}

// Complete workflow:
const delivery = await getDeliveryByOrderId(1);
if (delivery.data) {
    await updateDeliveryStatus(delivery.data.deliveryId, "DELIVERED");
}
```

---

## 🔄 Correct Workflow Examples

### Example 1: Mark Order as Delivered
```javascript
async function markOrderAsDelivered(orderId) {
    try {
        // Step 1: Get delivery information
        const deliveryResponse = await api.get(`/api/deliveries/order/${orderId}`);
        const delivery = deliveryResponse.data;
        
        // Step 2: Update delivery status to DELIVERED
        await api.put(`/api/deliveries/${delivery.deliveryId}/status`, {
            status: "DELIVERED"
        });
        
        // Step 3: Order status automatically becomes COMPLETED
        // No manual order status update needed!
        
        console.log("Delivery marked as completed, reviews are now enabled!");
    } catch (error) {
        console.error("Error marking order as delivered:", error);
    }
}
```

### Example 2: Mark Collection Order as Complete
```javascript
async function markOrderAsCollected(orderId) {
    try {
        const deliveryResponse = await api.get(`/api/deliveries/order/${orderId}`);
        const delivery = deliveryResponse.data;
        
        // Update delivery status to COLLECTED
        await api.put(`/api/deliveries/${delivery.deliveryId}/status`, {
            status: "COLLECTED"
        });
        
        // Order automatically becomes COMPLETED, reviews enabled!
    } catch (error) {
        console.error("Error marking order as collected:", error);
    }
}
```

---

## 🛠️ Frontend Code Updates Needed

### 1. API Client Updates
```javascript
// api.js - Update these functions

// ❌ Remove this function (wrong endpoint)
// async function updateOrderDeliveryStatus(orderId, status) { ... }

// ✅ Add these correct functions
async function updateDeliveryStatus(deliveryId, status) {
    return await api.put(`/api/deliveries/${deliveryId}/status`, { status });
}

async function getDeliveryByOrderId(orderId) {
    return await api.get(`/api/deliveries/order/${orderId}`);
}

async function updateOrderStatus(orderId, status) {
    return await api.put(`/api/orders/${orderId}/status`, { status });
}
```

### 2. Component Updates
```javascript
// OrdersView.vue or similar - Update these methods

async function updateDeliveryStatus(orderId, newStatus) {
    try {
        // Get delivery ID first
        const deliveryResponse = await getDeliveryByOrderId(orderId);
        const delivery = deliveryResponse.data;
        
        // Update delivery status (not order status!)
        await updateDeliveryStatus(delivery.deliveryId, newStatus);
        
        // Refresh order list to see automatic changes
        await fetchOrders();
    } catch (error) {
        console.error("Error updating delivery status:", error);
    }
}
```

### 3. Status Dropdown Updates
```javascript
// Make sure dropdowns use correct status values

const DELIVERY_STATUSES = [
    'PENDING',
    'IN_TRANSIT', 
    'OUT_FOR_DELIVERY',
    'DELIVERED',
    'COLLECTED',
    'READY_FOR_COLLECTION',
    'FAILED_DELIVERY',
    'RETURNED'
];

const ORDER_STATUSES = [
    'PENDING',
    'CONFIRMED',
    'PROCESSING', 
    'SHIPPED',
    'COMPLETED',
    'CANCELLED'
];
```

---

## ⚡ Quick Fix Checklist

### Immediate Changes:
- [ ] **Fix API endpoint**: `/api/orders/{id}/delivery/status` → `/api/deliveries/{id}/status`
- [ ] **Fix status values**: Don't use `CONFIRMED` for delivery status
- [ ] **Get delivery ID**: Use `/api/deliveries/order/{orderId}` to get delivery ID first
- [ ] **Update components**: Fix all components calling the wrong endpoint
- [ ] **Fix status dropdowns**: Use correct status values for each type

### Testing:
- [ ] Test delivery status updates work
- [ ] Verify order status automatically updates to `COMPLETED`
- [ ] Confirm reviews become available automatically
- [ ] Check admin panel works with correct endpoints

---

## 🎯 The Bottom Line

**Two separate systems:**
1. **Order Status** - Business workflow (`CONFIRMED`, `COMPLETED`, etc.)
2. **Delivery Status** - Logistics tracking (`DELIVERED`, `COLLECTED`, etc.)

**Two separate endpoints:**
1. **Order updates**: `PUT /api/orders/{id}/status`
2. **Delivery updates**: `PUT /api/deliveries/{id}/status`

**Automatic synchronization:**
- When delivery becomes `DELIVERED` or `COLLECTED`
- Order automatically becomes `COMPLETED`
- Reviews automatically become available

Fix these endpoint and status issues and the system will work perfectly!