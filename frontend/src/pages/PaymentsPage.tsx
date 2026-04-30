import { useEffect, useState } from 'react'
import { type AxiosResponse } from 'axios'
import { motion } from 'framer-motion'
import { Wallet, CreditCard, Plus, ArrowUpRight, ArrowDownLeft, Clock, Download } from 'lucide-react'
import { billingApi, paymentApi } from '../services/api'
import toast from 'react-hot-toast'

const SAMPLE_INVOICES = [
  { id: 'inv001', invoiceNumber: 'INV-EV-1234001', energyKwh: 22.5,
    subtotal: 315.00, cgstAmount: 28.35, sgstAmount: 28.35, totalAmount: 371.70,
    status: 'PAID', billingStart: '2026-04-28T14:00:00Z' },
  { id: 'inv002', invoiceNumber: 'INV-EV-1234002', energyKwh: 15.2,
    subtotal: 212.80, cgstAmount: 19.15, sgstAmount: 19.15, totalAmount: 251.10,
    status: 'PAID', billingStart: '2026-04-25T09:30:00Z' },
  { id: 'inv003', invoiceNumber: 'INV-EV-1234003', energyKwh: 45.0,
    subtotal: 630.00, cgstAmount: 56.70, sgstAmount: 56.70, totalAmount: 743.40,
    status: 'PENDING', billingStart: '2026-04-30T11:00:00Z' },
]

const SAMPLE_TXS = [
  { id: 'w1', type: 'TOPUP', amount: 500, description: 'Wallet top-up via UPI', createdAt: '2026-04-29T10:00:00Z' },
  { id: 'w2', type: 'DEBIT', amount: 371.70, description: 'Payment for INV-EV-1234001', createdAt: '2026-04-28T15:30:00Z' },
  { id: 'w3', type: 'DEBIT', amount: 251.10, description: 'Payment for INV-EV-1234002', createdAt: '2026-04-25T10:00:00Z' },
]

export default function PaymentsPage() {
  const [tab, setTab] = useState<'invoices' | 'wallet'>('invoices')
  const [invoices, setInvoices] = useState(SAMPLE_INVOICES)
  const [wallet, setWallet] = useState({ balance: 377.20 })
  const [topUpAmt, setTopUpAmt] = useState('')
  const [topUpLoading, setTopUpLoading] = useState(false)

  useEffect(() => {
    billingApi.getMyInvoices().then((r: AxiosResponse) => setInvoices(r.data.content || [])).catch(() => {})
    paymentApi.getWallet().then((r: AxiosResponse) => setWallet(r.data)).catch(() => {})
  }, [])

  const handleTopUp = async () => {
    const amount = parseFloat(topUpAmt)
    if (!amount || amount < 100) { toast.error('Minimum top-up ₹100'); return }
    setTopUpLoading(true)
    try {
      const res = await paymentApi.topUpWallet({ amount, paymentMethod: 'UPI' })
      setWallet(res.data)
      setTopUpAmt('')
      toast.success(`₹${amount} added to wallet!`)
    } catch { toast.error('Top-up failed') }
    setTopUpLoading(false)
  }

  return (
    <div className="p-6 max-w-4xl mx-auto animate-fade-in">
      <h1 className="font-display font-bold text-2xl mb-6">Payments & Billing</h1>

      {/* Wallet card */}
      <div className="glass-card p-6 mb-6 neon-border">
        <div className="flex items-center justify-between flex-wrap gap-4">
          <div>
            <p className="text-white/50 text-sm mb-1 flex items-center gap-1.5"><Wallet size={14}/> Wallet Balance</p>
            <div className="font-display font-extrabold text-4xl text-gradient">₹{wallet.balance.toFixed(2)}</div>
          </div>
          <div className="flex items-center gap-2">
            <input id="topup-amount" type="number" value={topUpAmt} onChange={e => setTopUpAmt(e.target.value)}
                   placeholder="₹ 500" className="input-field w-28 py-2.5 text-sm" />
            <button id="topup-btn" onClick={handleTopUp} disabled={topUpLoading} className="btn-primary py-2.5">
              {topUpLoading
                ? <span className="w-4 h-4 border-2 border-dark-900/30 border-t-dark-900 rounded-full animate-spin"/>
                : <><Plus size={16}/> Top Up</>}
            </button>
          </div>
        </div>
        <div className="flex gap-2 mt-3">
          {[200,500,1000,2000].map(a => (
            <button key={a} onClick={() => setTopUpAmt(a.toString())}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors
                      ${topUpAmt===String(a) ? 'bg-primary-400/20 text-primary-300 border border-primary-400/40'
                        : 'bg-dark-600 text-white/50 hover:text-white'}`}>₹{a}</button>
          ))}
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 p-1 bg-dark-700/50 rounded-xl mb-5 w-fit">
        {(['invoices','wallet'] as const).map(t => (
          <button key={t} onClick={() => setTab(t)}
                  className={`px-5 py-2 rounded-lg text-sm font-medium transition-all capitalize
                    ${tab===t ? 'bg-primary-400/20 text-primary-300 border border-primary-400/20' : 'text-white/50 hover:text-white'}`}>
            {t === 'invoices' ? '📄 Invoices' : '💳 Transactions'}
          </button>
        ))}
      </div>

      {tab === 'invoices' && (
        <div className="space-y-4">
          {invoices.map((inv, i) => (
            <motion.div key={inv.id} initial={{ opacity:0, y:20 }} animate={{ opacity:1, y:0 }}
                        transition={{ delay: i*0.08 }} className="glass-card p-5">
              <div className="flex items-start justify-between gap-3 mb-4 flex-wrap">
                <div>
                  <p className="font-mono text-primary-300 text-sm font-bold">{inv.invoiceNumber}</p>
                  <p className="text-white/40 text-xs mt-0.5 flex items-center gap-1">
                    <Clock size={11}/>
                    {new Date(inv.billingStart).toLocaleDateString('en-IN', { day:'2-digit', month:'long', year:'numeric', hour:'2-digit', minute:'2-digit'})}
                  </p>
                </div>
                <div className="text-right">
                  <div className="font-display font-bold text-xl text-ev-charge">₹{inv.totalAmount.toFixed(2)}</div>
                  <span className={inv.status==='PAID' ? 'badge-available text-xs' : 'badge-busy text-xs'}>{inv.status}</span>
                </div>
              </div>
              <div className="grid grid-cols-4 gap-3 mb-4">
                {[['Energy',`${inv.energyKwh?.toFixed(1)} kWh`],['Subtotal',`₹${inv.subtotal?.toFixed(2)}`],
                  ['CGST 9%',`₹${inv.cgstAmount?.toFixed(2)}`],['SGST 9%',`₹${inv.sgstAmount?.toFixed(2)}`]].map(([l,v]) => (
                  <div key={l} className="bg-dark-700/50 rounded-lg p-2.5 text-center">
                    <div className="font-medium text-sm">{v}</div>
                    <div className="text-xs text-white/40">{l}</div>
                  </div>
                ))}
              </div>
              <div className="flex gap-2">
                {inv.status === 'PENDING' && (
                  <button id={`pay-${inv.id}`} className="btn-primary py-2 text-sm flex-1 justify-center">
                    <CreditCard size={14}/> Pay via UPI
                  </button>
                )}
                <button className="btn-ghost py-2 text-sm"><Download size={14}/> PDF</button>
              </div>
            </motion.div>
          ))}
        </div>
      )}

      {tab === 'wallet' && (
        <div className="space-y-3">
          {SAMPLE_TXS.map((tx, i) => (
            <motion.div key={tx.id} initial={{ opacity:0, y:20 }} animate={{ opacity:1, y:0 }}
                        transition={{ delay: i*0.08 }} className="glass-card p-4 flex items-center gap-4">
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 ${
                tx.type==='TOPUP' ? 'bg-green-500/15 border border-green-500/30' : 'bg-red-500/15 border border-red-500/30'}`}>
                {tx.type==='TOPUP'
                  ? <ArrowDownLeft size={18} className="text-ev-charge"/>
                  : <ArrowUpRight size={18} className="text-red-400"/>}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">{tx.description}</p>
                <p className="text-xs text-white/40 flex items-center gap-1 mt-0.5">
                  <Clock size={10}/> {new Date(tx.createdAt).toLocaleDateString('en-IN')}
                </p>
              </div>
              <div className={`font-bold text-lg flex-shrink-0 ${tx.type==='TOPUP' ? 'text-ev-charge' : 'text-red-400'}`}>
                {tx.type==='TOPUP' ? '+' : '-'}₹{tx.amount.toFixed(2)}
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  )
}
