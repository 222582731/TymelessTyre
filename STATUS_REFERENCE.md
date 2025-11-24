# Status Reference Guide

## 📊 Quick Status Mapping

### Order Status (Business Workflow)
| Status | Description | Customer Action | Review Eligible |
|--------|-------------|-----------------|------------------|
| `PENDING` | Order created, payment pending | Complete payment | ❌ |
| `CONFIRMED` | Payment received | Wait for processing | ❌ |
| `PROCESSING` | Order being prepared/packed | Track progress | ❌ |
| `SHIPPED` | Order shipped/ready for pickup | Receive delivery | ❌ |
| `COMPLETED` | **Order fully completed** | **Write reviews** | ✅ |
| `CANCELLED` | Order cancelled | - | ❌ |

### Delivery Status (Logistics Tracking)
| Status | Description | Automatic Order Update |
|--------|-------------|------------------------|
| `PENDING` | Awaiting dispatch | - |
| `IN_TRANSIT` | On the way to customer | - |
| `OUT_FOR_DELIVERY` | Out for final delivery | - |
| `DELIVERED` | Successfully delivered | → `COMPLETED` ✨ |
| `COLLECTED` | Picked up from store | → `COMPLETED` ✨ |
| `READY_FOR_COLLECTION` | Ready for customer pickup | - |
| `FAILED_DELIVERY` | Delivery failed | - |
| `RETURNED` | Returned to sender | - |

### ⚠️ CRITICAL: Status Types Are Different!
- **Order Status**: `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `COMPLETED`, `CANCELLED`
- **Delivery Status**: `PENDING`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `COLLECTED`, `READY_FOR_COLLECTION`, `FAILED_DELIVERY`, `RETURNED`

**DO NOT mix these!** `CONFIRMED` is NOT a delivery status!

## 🔄 Status Flow Examples

### Home Delivery Flow:
```
Order: PENDING → CONFIRMED → PROCESSING → SHIPPED → COMPLETED
Delivery: PENDING → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
                                                      ↓
                                    Auto-triggers: Order = COMPLETED
                                                      ↓
                                              Reviews Enabled ✅
```

### Store Pickup Flow:
```
Order: PENDING → CONFIRMED → PROCESSING → SHIPPED → COMPLETED
Delivery: PENDING → READY_FOR_PICKUP → COLLECTED
                                          ↓
                        Auto-triggers: Order = COMPLETED
                                          ↓
                                Reviews Enabled ✅
```

## 🎯 Frontend Implementation Points

### Critical Changes:
1. **Replace** `"delivered"` with `"COMPLETED"`
2. **Use uppercase** enum values
3. **Automatic review enablement** - no manual triggers needed
4. **Real-time updates** when delivery completes

### Review Button Logic:
```javascript
// Simple and reliable
const canReview = order.orderStatus === 'COMPLETED';
```

### Status Display Priority:
1. **Primary**: Order Status (what customer cares about)
2. **Secondary**: Delivery Status (detailed tracking)
3. **Context**: Payment Status (if relevant)

## 🚀 Benefits of New System

- ✅ **Automatic**: Reviews enable without manual intervention
- ✅ **Consistent**: No more status sync issues
- ✅ **Clear**: Separate business vs logistics tracking
- ✅ **Reliable**: Enum validation prevents errors
- ✅ **Real-time**: Immediate updates when delivery completes