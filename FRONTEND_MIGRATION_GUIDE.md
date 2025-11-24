# Frontend Migration Guide: Order Status & Review System

## 🎯 Overview

The backend has been refactored to properly separate **Order Status** (business workflow) from **Delivery Status** (logistics tracking) and automatically enable reviews when orders are completed. This guide covers all frontend changes needed.

---

## 📋 Table of Contents

1. [Order Status Changes](#order-status-changes)
2. [Review System Changes](#review-system-changes)
3. [API Endpoint Changes](#api-endpoint-changes)
4. [UI/UX Updates Required](#uiux-updates-required)
5. [Status Display Guidelines](#status-display-guidelines)
6. [Error Handling Updates](#error-handling-updates)
7. [Testing Considerations](#testing-considerations)

---

## 🔄 Order Status Changes

### **OLD vs NEW Order Status Values**

| **OLD String Values** | **NEW Enum Values** | **Description** |
|----------------------|---------------------|-----------------|
| `"pending"` | `"PENDING"` | Order created, awaiting payment |
| `"confirmed"` | `"CONFIRMED"` | Payment received, order confirmed |
| `"processing"` | `"PROCESSING"` | Order being prepared/packed |
| `"shipped"` | `"SHIPPED"` | Order shipped/ready for collection |
| `"delivered"` ⚠️ | `"COMPLETED"` ✨ | **Order completed (enables reviews)** |
| `"cancelled"` | `"CANCELLED"` | Order cancelled |

### **⚠️ CRITICAL CHANGES:**
- **`"delivered"` → `"COMPLETED"`**: This is the key change for review eligibility
- All status values are now **UPPERCASE**
- **`"PROCESSING"`** is new - use between `CONFIRMED` and `SHIPPED`

---

## 🎉 Review System Changes

### **Review Eligibility Logic**

**OLD Logic:**
```javascript
// ❌ OLD - Don't use this anymore
function canReview(order) {
    return order.orderStatus?.toLowerCase() === 'delivered';
}
```

**NEW Logic:**
```javascript
// ✅ NEW - Use this
function canReview(order) {
    return order.orderStatus === 'COMPLETED';
}
```

### **Automatic Review Enablement**

The backend now **automatically** updates order status to `COMPLETED` when:
- Delivery status becomes `DELIVERED` (for home delivery)
- Delivery status becomes `COLLECTED` (for store pickup)

**Frontend Impact:**
- **No manual status updates needed** for review enablement
- Reviews become available **immediately** after delivery completion
- Real-time status updates should reflect automatic changes

---

## 🔌 API Endpoint Changes

### **⚠️ CRITICAL: Two Separate Systems**

There are **TWO DIFFERENT** status systems with **DIFFERENT ENDPOINTS**:

#### **1. Order Status Updates** (Business Workflow)
**Endpoint:** `PUT /api/orders/{id}/status`

**Valid Order Status Values:**
- `"PENDING"` - Order created, awaiting payment
- `"CONFIRMED"` - Payment received ⚠️ **Order status only!**
- `"PROCESSING"` - Order being prepared
- `"SHIPPED"` - Order shipped/ready
- `"COMPLETED"` - Order completed (enables reviews)
- `"CANCELLED"` - Order cancelled

**Request Body:**
```json
{
    "status": "COMPLETED"
}
```

#### **2. Delivery Status Updates** (Logistics Tracking)
**Endpoint:** `PUT /api/deliveries/{deliveryId}/status`

**Valid Delivery Status Values:**
- `"PENDING"` - Awaiting dispatch
- `"IN_TRANSIT"` - On the way
- `"OUT_FOR_DELIVERY"` - Out for delivery
- `"DELIVERED"` - Successfully delivered → **Auto-updates Order to `COMPLETED`**
- `"COLLECTED"` - Picked up → **Auto-updates Order to `COMPLETED`**
- `"READY_FOR_COLLECTION"` - Ready for pickup
- `"FAILED_DELIVERY"` - Delivery failed
- `"RETURNED"` - Returned to sender

**Request Body:**
```json
{
    "status": "DELIVERED"
}
```

**⚠️ Common Mistake:** 
- **`CONFIRMED` is NOT a delivery status!** It's an order status only.
- Use correct endpoint: `/api/deliveries/{id}/status` (not `/api/orders/{id}/delivery/status`)

### **3. Get Delivery by Order ID**

**Endpoint:** `GET /api/deliveries/order/{orderId}`

**Response:**
```json
{
    "deliveryId": 5,
    "deliveryStatus": "DELIVERED",
    "deliveryMethod": "HOME_DELIVERY",
    "courierName": "DHL Express",
    "order": {
        "orderId": 123,
        "orderStatus": "COMPLETED"
    }
}
```

### **4. Get Payment by Order ID**

**Endpoint:** `GET /api/payments/order/{orderId}`

**Response:**
```json
{
    "paymentId": 3,
    "paymentStatus": "CONFIRMED",
    "paymentMethod": "CREDIT_CARD",
    "order": {
        "orderId": 123,
        "orderStatus": "CONFIRMED"
    }
}
```

### **5. Update Payment Status**

**Endpoint:** `PUT /api/payments/{paymentId}/status`

**Valid Payment Status Values:**
- `"PENDING"` - Payment pending
- `"CONFIRMED"` - Payment confirmed
- `"COMPLETED"` - Payment completed
- `"FAILED"` - Payment failed
- `"REFUNDED"` - Payment refunded

**Request Body:**
```json
{
    "status": "CONFIRMED"
}
```

### **6. Complete Update Workflows**

**To update delivery status:**
```javascript
// Step 1: Get delivery information
const deliveryResponse = await api.get(`/api/deliveries/order/${orderId}`);
const delivery = deliveryResponse.data;

// Step 2: Update delivery status
await api.put(`/api/deliveries/${delivery.deliveryId}/status`, {
    status: "DELIVERED" // This automatically updates order to COMPLETED
});
```

**To update payment status:**
```javascript
// Step 1: Get payment information
const paymentResponse = await api.get(`/api/payments/order/${orderId}`);
const payment = paymentResponse.data;

// Step 2: Update payment status
await api.put(`/api/payments/${payment.paymentId}/status`, {
    status: "CONFIRMED"
});
```

---

## 🎨 UI/UX Updates Required

### **1. Order Status Display**

**Status Badge Component Updates:**

```typescript
// ✅ NEW Status Display Function
function getOrderStatusDisplay(status: string) {
    const statusMap = {
        'PENDING': {
            label: 'Pending Payment',
            color: 'orange',
            icon: '⏳'
        },
        'CONFIRMED': {
            label: 'Order Confirmed',
            color: 'blue',
            icon: '✅'
        },
        'PROCESSING': {
            label: 'Being Prepared',
            color: 'purple',
            icon: '📦'
        },
        'SHIPPED': {
            label: 'Shipped/Ready',
            color: 'teal',
            icon: '🚚'
        },
        'COMPLETED': {
            label: 'Completed',
            color: 'green',
            icon: '🎉'
        },
        'CANCELLED': {
            label: 'Cancelled',
            color: 'red',
            icon: '❌'
        }
    };
    
    return statusMap[status] || {
        label: status,
        color: 'gray',
        icon: '❓'
    };
}
```

### **2. Review Button Logic**

**Component Example:**
```tsx
function ReviewButton({ order, product }) {
    const canReview = order.orderStatus === 'COMPLETED';
    
    return (
        <button 
            disabled={!canReview}
            className={canReview ? 'btn-primary' : 'btn-disabled'}
        >
            {canReview ? 'Write Review' : 'Complete order to review'}
        </button>
    );
}
```

### **3. Order Progress Indicator**

**Updated Progress Steps:**
```typescript
const ORDER_PROGRESS_STEPS = [
    { key: 'PENDING', label: 'Order Placed', description: 'Awaiting payment' },
    { key: 'CONFIRMED', label: 'Payment Confirmed', description: 'Order confirmed' },
    { key: 'PROCESSING', label: 'Preparing', description: 'Being prepared' },
    { key: 'SHIPPED', label: 'Shipped/Ready', description: 'On the way or ready for pickup' },
    { key: 'COMPLETED', label: 'Completed', description: 'Delivered or collected' }
];
```

---

## 📊 Status Display Guidelines

### **Order Status vs Delivery Status**

Display **both** statuses for complete transparency:

```tsx
function OrderStatusCard({ order }) {
    return (
        <div className="order-status-card">
            {/* Primary Status - Order Business State */}
            <div className="primary-status">
                <h3>Order Status</h3>
                <StatusBadge status={order.orderStatus} />
                {order.orderStatus === 'COMPLETED' && (
                    <ReviewPrompt orderId={order.orderId} />
                )}
            </div>
            
            {/* Secondary Status - Delivery Tracking */}
            {order.delivery && (
                <div className="delivery-status">
                    <h4>Delivery Tracking</h4>
                    <DeliveryStatusBadge status={order.delivery.deliveryStatus} />
                    <p>Courier: {order.delivery.courierName}</p>
                </div>
            )}
        </div>
    );
}
```

### **Status Priority Display**

**Show the most relevant status to customers:**

1. **Primary**: Order Status (business workflow)
2. **Secondary**: Delivery Status (detailed tracking)
3. **Context**: Payment Status (if needed)

---

## 🚨 Error Handling Updates

### **Invalid Status Values**

Handle new validation errors:

```typescript
function handleStatusUpdate(orderId: number, newStatus: string) {
    try {
        const response = await updateOrderStatus(orderId, newStatus);
        return response;
    } catch (error) {
        if (error.status === 400) {
            // New error format for invalid enum values
            showError(`Invalid status. Valid options: PENDING, CONFIRMED, PROCESSING, SHIPPED, COMPLETED, CANCELLED`);
        }
        throw error;
    }
}
```

### **Status Transition Validation**

The backend now validates status transitions:

```typescript
// Handle transition validation errors
const INVALID_TRANSITIONS = {
    'COMPLETED': 'Order is already completed and cannot be changed',
    'CANCELLED': 'Order is cancelled and cannot be changed'
};
```

---

## 🔄 Automatic Status Updates

### **Real-time Updates**

Since order status updates automatically when delivery is completed, implement real-time updates:

```typescript
// WebSocket or polling for order status changes
function subscribeToOrderUpdates(orderId: number) {
    // When delivery status changes to DELIVERED/COLLECTED
    // Order status automatically becomes COMPLETED
    // Update UI immediately to show review options
    
    socket.on(`order-${orderId}-updated`, (updatedOrder) => {
        if (updatedOrder.orderStatus === 'COMPLETED') {
            // Show success message
            showNotification('Order completed! You can now write reviews.');
            // Enable review buttons
            enableReviewButtons(orderId);
        }
    });
}
```

---

## 🧪 Testing Considerations

### **Test Cases to Update**

1. **Order Status Display Tests**
   ```typescript
   // Update test expectations
   expect(orderStatusBadge).toHaveText('COMPLETED'); // not 'delivered'
   ```

2. **Review Eligibility Tests**
   ```typescript
   // Update review button tests
   const order = { orderStatus: 'COMPLETED' }; // not 'delivered'
   expect(canWriteReview(order)).toBe(true);
   ```

3. **Status Transition Tests**
   ```typescript
   // Test automatic status updates
   // When delivery becomes DELIVERED → order becomes COMPLETED
   ```

### **Integration Testing**

Test the full flow:
1. Create order (`PENDING`)
2. Confirm payment (`CONFIRMED`)
3. Start processing (`PROCESSING`)
4. Ship order (`SHIPPED`)
5. Complete delivery → **Auto-update to `COMPLETED`**
6. Verify reviews are enabled

---

## 📱 Mobile App Considerations

### **Push Notifications**

Update notification messages:

```typescript
const NOTIFICATION_MESSAGES = {
    'CONFIRMED': 'Your order has been confirmed!',
    'PROCESSING': 'Your order is being prepared',
    'SHIPPED': 'Your order is on the way!',
    'COMPLETED': 'Order completed! Write a review and help others.' // Updated
};
```

### **Offline Handling**

Cache status mappings for offline scenarios:

```typescript
const STATUS_CACHE = {
    // Include all new status values for offline display
    'COMPLETED': 'Order Completed',
    // ... other statuses
};
```

---

## 🔧 Implementation Checklist

### **Frontend Components to Update:**

- [ ] **Order Status Badge Component**
- [ ] **Order List/Grid Components**
- [ ] **Order Detail Page**
- [ ] **Review Button Component**
- [ ] **Order Progress Indicator**
- [ ] **Admin Order Management**
- [ ] **Customer Order History**
- [ ] **Review Eligibility Checks**

### **API Integration Updates:**

- [ ] **Update all order status comparisons**
- [ ] **Handle new enum validation errors**
- [ ] **Update order status update calls**
- [ ] **Test review eligibility API changes**

### **Testing Updates:**

- [ ] **Unit tests for status display**
- [ ] **Integration tests for review flow**
- [ ] **E2E tests for complete order journey**

---

## 🎯 Summary

**Key Changes for Frontend:**

1. **Status Values**: Use uppercase enum values (`COMPLETED` not `delivered`)
2. **Review Logic**: Check for `orderStatus === 'COMPLETED'`
3. **Automatic Updates**: Implement real-time status updates
4. **UI Updates**: Update all status display components
5. **Error Handling**: Handle new validation errors

**Benefits:**
- ✅ **Automatic review enablement** - no manual intervention needed
- ✅ **Better user experience** - clear status progression
- ✅ **Type safety** - enum validation prevents errors
- ✅ **Real-time updates** - customers see changes immediately

The backend now handles the complex business logic automatically, making the frontend implementation cleaner and more reliable!